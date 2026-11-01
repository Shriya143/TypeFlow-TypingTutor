package typingtutor;

import javax.swing.*;
import java.awt.*;

/** The main window: colourful header, navigation buttons, and three switchable screens. */
public class MainFrame extends JFrame {
    public MainFrame(String user, StatsRepository repo) {
        super("TypeFlow - Typing Tutor");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 720);
        setMinimumSize(new Dimension(880, 600));
        setLocationRelativeTo(null);
        setContentPane(new FunBackground());

        CardLayout cards = new CardLayout();
        JPanel screens = new JPanel(cards);
        screens.setOpaque(false);
        DashboardPanel dashboard = new DashboardPanel(user, repo);
        LeaderboardPanel board = new LeaderboardPanel(user, repo);
        TypingPanel typing = new TypingPanel(user, repo, () -> { dashboard.refresh(); board.refresh(); });
        screens.add(typing, "practice");
        screens.add(dashboard, "dashboard");
        screens.add(board, "board");

        JButton practiceBtn = new JButton("Practice");
        JButton dashBtn = new JButton("My Stats");
        JButton boardBtn = new JButton("Leaderboard");
        TypingPanel.styleButton(practiceBtn, Theme.YELLOW, Color.BLACK);
        TypingPanel.styleButton(dashBtn, Theme.ACCENT, Color.BLACK);
        TypingPanel.styleButton(boardBtn, Theme.PINK, Color.BLACK);
        practiceBtn.addActionListener(e -> cards.show(screens, "practice"));
        dashBtn.addActionListener(e -> { dashboard.refresh(); cards.show(screens, "dashboard"); });
        boardBtn.addActionListener(e -> { board.refresh(); cards.show(screens, "board"); });

        JLabel title = new JLabel("<html><span style='color:#FACC15'>T</span><span style='color:#F472B6'>y</span>"
                + "<span style='color:#38BDF8'>p</span><span style='color:#4ADE80'>e</span>"
                + "<span style='color:#FB923C'>F</span><span style='color:#C084FC'>l</span>"
                + "<span style='color:#FACC15'>o</span><span style='color:#38BDF8'>w</span></html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        JLabel who = new JLabel("Player: " + user);
        who.setForeground(Color.WHITE);
        who.setFont(Theme.BOLD);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nav.setOpaque(false);
        nav.add(title);
        nav.add(Box.createHorizontalStrut(20));
        nav.add(practiceBtn);
        nav.add(dashBtn);
        nav.add(boardBtn);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 0, 24));
        header.add(nav, BorderLayout.WEST);
        header.add(who, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(screens, BorderLayout.CENTER);
    }
}
