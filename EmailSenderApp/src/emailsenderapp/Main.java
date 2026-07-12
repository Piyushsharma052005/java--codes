package emailsenderapp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the Email Sender GUI application.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore - fall back to default look and feel
            }
            EmailSenderFrame frame = new EmailSenderFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
