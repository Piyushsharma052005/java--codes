package emailsenderapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * Minimal SMTP client that talks directly to a mail server over an
 * SSL/TLS socket (implicit TLS, e.g. port 465). No external mail
 * library (JavaMail/Jakarta Mail) is required - everything is done
 * with plain JDK sockets and the SMTP text protocol.
 *
 * Works out of the box with providers that support SMTPS on port 465,
 * such as Gmail (smtp.gmail.com:465) - remember to use an "App
 * Password" for Gmail, not your normal account password.
 */
public class SmtpMailer {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final Consumer<String> logger;

    public SmtpMailer(String host, int port, String username, String password, Consumer<String> logger) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.logger = logger;
    }

    public void send(String toAddress, String subject, String body) throws IOException {
          TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }
    };

    SSLContext sc;
    try {
        sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
    } catch (Exception e) {
        throw new IOException(e);
    }

    SSLSocketFactory factory = sc.getSocketFactory();

    try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {

         socket.startHandshake();


       
        
            
            
            socket.setEnabledProtocols(new String[]{"TLSv1.2"});

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream out = socket.getOutputStream();

            readResponse(in, 220);
            sendCommand(out, in, "EHLO localhost", 250);
            sendCommand(out, in, "AUTH LOGIN", 334);
            sendCommand(out, in, Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8)), 334);
            sendCommand(out, in, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)), 235);
            sendCommand(out, in, "MAIL FROM:<" + username + ">", 250);
            sendCommand(out, in, "RCPT TO:<" + toAddress + ">", 250);
            sendCommand(out, in, "DATA", 354);

            StringBuilder message = new StringBuilder();
            message.append("From: ").append(username).append("\r\n");
            message.append("To: ").append(toAddress).append("\r\n");
            message.append("Subject: ").append(subject).append("\r\n");
            message.append("MIME-Version: 1.0\r\n");
            message.append("Content-Type: text/plain; charset=UTF-8\r\n");
            message.append("\r\n");
            // Escape lines starting with a lone "." per SMTP rules
            for (String line : body.split("\n", -1)) {
                if (line.startsWith(".")) {
                    message.append('.');
                }
                message.append(line).append("\r\n");
            }
            message.append(".\r\n");

            write(out, message.toString());
            readResponse(in, 250);

            sendCommand(out, in, "QUIT", 221);
        }
    }

    private void sendCommand(OutputStream out, BufferedReader in, String command, int expectedCode) throws IOException {
        String toLog = command.startsWith("AUTH LOGIN") || looksLikeBase64Credential(command) ? "> [credentials hidden]" : "> " + command;
        log(toLog);
        write(out, command + "\r\n");
        readResponse(in, expectedCode);
    }

    private boolean looksLikeBase64Credential(String s) {
        // Heuristic only used for prettifying the log output.
        return s.matches("^[A-Za-z0-9+/=]+$") && s.length() > 4;
    }

    private void write(OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private String readResponse(BufferedReader in, int expectedCode) throws IOException {
        String line;
        StringBuilder full = new StringBuilder();
        int code = -1;
        boolean multiLine = true;
        while (multiLine) {
            line = in.readLine();
            if (line == null) {
                throw new IOException("Server closed the connection unexpectedly.");
            }
            full.append(line).append("\n");
            if (line.length() >= 4 && line.charAt(3) == ' ') {
                code = Integer.parseInt(line.substring(0, 3));
                multiLine = false;
            }
        }
        log("< " + full.toString().trim());
        if (code != expectedCode) {
            throw new IOException("Unexpected SMTP response (expected " + expectedCode + "): " + full);
        }
        return full.toString();
    }

    private void log(String msg) {
        if (logger != null) {
            logger.accept(msg);
        }
    }
}
