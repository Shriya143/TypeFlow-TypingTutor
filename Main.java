package typingtutor;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }

            String user = JOptionPane.showInputDialog(null, "Enter your player name:", "Welcome to TypeFlow",
                    JOptionPane.QUESTION_MESSAGE);
            if (user == null) return;                 // user pressed Cancel
            user = user.trim().isEmpty() ? "guest" : user.trim();

            new MainFrame(user, new FileStatsRepository()).setVisible(true);
        });
    }
}
