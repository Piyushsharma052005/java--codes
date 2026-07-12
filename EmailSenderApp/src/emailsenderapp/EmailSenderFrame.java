package emailsenderapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

/**
 * A simple, good-looking desktop GUI for sending emails.
 *
 * Sending is implemented in {@link SmtpMailer} using a plain SSL
 * socket talking raw SMTP - no external JavaMail jar is required,
 * so the project compiles and runs with nothing more than the JDK.
 */
public class EmailSenderFrame extends JFrame {

    // ---- Color palette ----
    private static final Color COLOR_HEADER = new Color(0x2C3E50);
    private static final Color COLOR_HEADER_TEXT = Color.WHITE;
    private static final Color COLOR_BG = new Color(0xF4F6F8);
    private static final Color COLOR_ACCENT = new Color(0x27AE60);
    private static final Color COLOR_ACCENT_HOVER = new Color(0x219150);
    private static final Color COLOR_FIELD_BORDER = new Color(0xD0D5DA);
    private static final Color COLOR_TEXT = new Color(0x2C3E50);

    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField smtpHostField;
    private JTextField smtpPortField;
    private JTextField toField;
    private JTextField subjectField;
    private JTextArea bodyArea;
    private JTextArea logArea;
    private JButton sendButton;

    public EmailSenderFrame() {
        super("Email Sender");
        buildUi();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 720);
        setMinimumSize(new Dimension(640, 600));
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        root.add(buildLogPanel(), BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------
    // Header
    // ---------------------------------------------------------------
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("\u2709  Email Sender");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(COLOR_HEADER_TEXT);

        JLabel subtitle = new JLabel("Compose and send an email straight from your desktop");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0xBFC9D1));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    // ---------------------------------------------------------------
    // Form (account settings + compose area)
    // ---------------------------------------------------------------
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.setBorder(new EmptyBorder(20, 24, 10, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 14, 0);

        wrapper.add(buildCard("Sender Account", buildAccountFields()), c);

        c.gridy = 1;
        wrapper.add(buildCard("Compose Message", buildComposeFields()), c);

        c.gridy = 2;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JPanel sendRow = new JPanel(new BorderLayout());
        sendRow.setOpaque(false);
        sendRow.setBorder(new EmptyBorder(4, 0, 0, 0));
        sendButton = buildAccentButton("Send Email");
        sendButton.addActionListener(e -> onSendClicked());
        JPanel sendButtonHolder = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        sendButtonHolder.setOpaque(false);
        sendButtonHolder.add(sendButton);
        sendRow.add(sendButtonHolder, BorderLayout.NORTH);
        wrapper.add(sendRow, c);

        return wrapper;
    }

    private JPanel buildCard(String titleText, JPanel content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(COLOR_TEXT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAccountFields() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = baseConstraints();

        emailField = styledTextField();
        passwordField = styledPasswordField();
        smtpHostField = styledTextField();
        smtpHostField.setText("smtp.gmail.com");
        smtpPortField = styledTextField();
        smtpPortField.setText("465");

        addLabeledRow(panel, c, 0, "Your Email", emailField);
        addLabeledRow(panel, c, 1, "App Password", passwordField);
        addLabeledRow(panel, c, 2, "SMTP Host", smtpHostField);
        addLabeledRow(panel, c, 3, "SMTP Port", smtpPortField);

        JLabel hint = new JLabel("<html><i>Tip: For Gmail, enable 2-Step Verification and generate an "
                + "\"App Password\" - do not use your normal login password.</i></html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(new Color(0x8A93A0));
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.insets = new Insets(6, 0, 0, 0);
        panel.add(hint, c);

        return panel;
    }

    private JPanel buildComposeFields() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = baseConstraints();

        toField = styledTextField();
        subjectField = styledTextField();

        addLabeledRow(panel, c, 0, "To", toField);
        addLabeledRow(panel, c, 1, "Subject", subjectField);

        JLabel bodyLabel = new JLabel("Message");
        bodyLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        bodyLabel.setForeground(COLOR_TEXT);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.insets = new Insets(10, 0, 4, 0);
        panel.add(bodyLabel, c);

        bodyArea = new JTextArea(8, 20);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bodyArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER));

        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        panel.add(bodyScroll, c);

        return panel;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        c.weightx = 1.0;
        return c;
    }

    private void addLabeledRow(JPanel panel, GridBagConstraints c, int row, String labelText, javax.swing.JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT);
        label.setPreferredSize(new Dimension(110, label.getPreferredSize().height));

        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(field, c);
    }

    private JTextField styledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        return field;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        return field;
    }

    private JButton buildAccentButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(COLOR_ACCENT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 26, 10, 26));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addChangeListener(e -> {
            if (button.getModel().isRollover()) {
                button.setBackground(COLOR_ACCENT_HOVER);
            } else {
                button.setBackground(COLOR_ACCENT);
            }
        });
        return button;
    }

    // ---------------------------------------------------------------
    // Log panel
    // ---------------------------------------------------------------
    private JPanel buildLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(0, 24, 18, 24));

        JLabel label = new JLabel("Status");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT);
        label.setBorder(new EmptyBorder(0, 0, 6, 0));

        logArea = new JTextArea(6, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(0x1E1E1E));
        logArea.setForeground(new Color(0x00E676));
        logArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER));

        panel.add(label, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(new JSeparator(), BorderLayout.SOUTH);
        return panel;
    }

    // ---------------------------------------------------------------
    // Sending logic
    // ---------------------------------------------------------------
    private void onSendClicked() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String host = smtpHostField.getText().trim();
        String portText = smtpPortField.getText().trim();
        String to = toField.getText().trim();
        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();

        if (email.isEmpty() || password.isEmpty() || host.isEmpty() || portText.isEmpty() || to.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in your email, app password, SMTP host/port, and a recipient.",
                    "Missing information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "SMTP port must be a number.", "Invalid port", JOptionPane.WARNING_MESSAGE);
            return;
        }

        sendButton.setEnabled(false);
        sendButton.setText("Sending...");
        logArea.setText("");
        appendLog("Connecting to " + host + ":" + port + " ...");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                SmtpMailer mailer = new SmtpMailer(host, port, email, password, this::publish);
                mailer.send(to, subject, body);
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    appendLog(chunk);
                }
            }

            @Override
            protected void done() {
                sendButton.setEnabled(true);
                sendButton.setText("Send Email");
                try {
                    get();
                    appendLog("Email sent successfully!");
                    JOptionPane.showMessageDialog(EmailSenderFrame.this,
                            "Email sent successfully to " + to, "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    appendLog("ERROR: " + cause.getMessage());
                    JOptionPane.showMessageDialog(EmailSenderFrame.this,
                            "Failed to send email:\n" + cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void appendLog(String text) {
        logArea.append(text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
