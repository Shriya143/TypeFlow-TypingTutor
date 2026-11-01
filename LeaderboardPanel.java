package typingtutor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Hall of Fame: ranking of every player by best speed. */
public class LeaderboardPanel extends JPanel {
    private final String user;
    private final StatsRepository repo;
    private List<Ranking.PlayerStat> rows = new ArrayList<>();

    public LeaderboardPanel(String user, StatsRepository repo) {
        this.user = user;
        this.repo = repo;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        refresh();
    }

    public void refresh() {
        rows = Ranking.compute(repo.loadAll());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Insets in = getInsets();
        int x0 = in.left, y0 = in.top, W = getWidth() - in.left - in.right, H = getHeight() - in.top - in.bottom;

        g.setColor(Theme.CARD_ALPHA);
        g.fillRoundRect(x0, y0, W, H, 28, 28);
        int left = x0 + 28, right = x0 + W - 28;

        // title
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(Theme.YELLOW);
        g.drawString("HALL OF FAME", left + 44, y0 + 50);
        g.fill(FunBackground.star(left + 16, y0 + 40, 16));
        int tw = g.getFontMetrics().stringWidth("HALL OF FAME");
        g.fill(FunBackground.star(left + 44 + tw + 30, y0 + 40, 16));

        int rank = Ranking.rankOf(rows, user);
        g.setFont(Theme.UI);
        g.setColor(Color.WHITE);
        String sub = rank < 0 ? "Play a game to join the ranking!"
                : "You are ranked #" + rank + " of " + rows.size() + " player" + (rows.size() == 1 ? "" : "s");
        g.drawString(sub, left + 44, y0 + 74);

        // column positions
        int nameX = left + 56;
        int barX = nameX + 170;
        int barEnd = right - 370;
        int bestR = right - 290, accR = right - 210, gamesR = right - 130;

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(Theme.MUTED);
        g.drawString("PLAYER", nameX, y0 + 108);
        g.drawString("SPEED", barX, y0 + 108);
        drawRight(g, "BEST WPM", bestR, y0 + 108);
        drawRight(g, "ACCURACY", accR, y0 + 108);
        drawRight(g, "GAMES", gamesR, y0 + 108);
        drawRight(g, "TITLE", right, y0 + 108);

        int rowsTop = y0 + 120;
        int avail = y0 + H - 16 - rowsTop;
        if (rows.isEmpty()) {
            g.setFont(Theme.TITLE);
            g.setColor(Theme.TEXT);
            String msg = "Nobody has played yet. Be the first champion!";
            g.drawString(msg, left + (right - left - g.getFontMetrics().stringWidth(msg)) / 2, rowsTop + avail / 2);
            g.dispose();
            return;
        }

        int shown = Math.min(rows.size(), Math.max(1, Math.min(8, avail / 44)));
        int rowH = Math.min(58, avail / shown);
        double maxBest = Math.max(1, rows.get(0).best);

        for (int i = 0; i < shown; i++) {
            Ranking.PlayerStat p = rows.get(i);
            int ry = rowsTop + i * rowH;
            int cy = ry + rowH / 2;
            boolean me = p.name.equalsIgnoreCase(user);

            g.setColor(me ? new Color(250, 204, 21, 50) : new Color(255, 255, 255, 18));
            g.fillRoundRect(left - 10, ry + 3, right - left + 20, rowH - 6, 22, 22);
            if (me) {
                g.setColor(Theme.YELLOW);
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(left - 10, ry + 3, right - left + 20, rowH - 6, 22, 22);
            }

            // rank badge
            Color badge = i == 0 ? Theme.YELLOW : i == 1 ? new Color(0xCBD5E1) : i == 2 ? Theme.ORANGE : Theme.PURPLE;
            g.setColor(badge);
            g.fillOval(left + 4, cy - 17, 34, 34);
            g.setColor(new Color(255, 255, 255, 110));
            g.fillOval(left + 10, cy - 13, 12, 12);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(i < 3 ? new Color(0x1E1B4B) : Color.WHITE);
            String num = String.valueOf(i + 1);
            g.drawString(num, left + 21 - g.getFontMetrics().stringWidth(num) / 2, cy + 6);

            // name
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(Color.WHITE);
            String name = p.name.length() > 13 ? p.name.substring(0, 12) + "..." : p.name;
            g.drawString(name, nameX, cy + 6);

            // speed bar
            int trackW = barEnd - barX;
            if (trackW > 30) {
                g.setColor(new Color(255, 255, 255, 30));
                g.fillRoundRect(barX, cy - 8, trackW, 16, 16, 16);
                int fillW = Math.max(14, (int) (trackW * p.best / maxBest));
                g.setPaint(new GradientPaint(barX, 0, Theme.ACCENT, barX + fillW, 0, Theme.PINK));
                g.fillRoundRect(barX, cy - 8, fillW, 16, 16, 16);
            }

            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.setColor(Theme.YELLOW);
            drawRight(g, String.format("%.0f", p.best), bestR, cy + 6);
            g.setFont(Theme.BOLD);
            g.setColor(Theme.GOOD);
            drawRight(g, String.format("%.0f%%", p.avgAccuracy), accR, cy + 5);
            g.setColor(Color.WHITE);
            drawRight(g, String.valueOf(p.sessions), gamesR, cy + 5);
            g.setColor(Theme.PINK);
            drawRight(g, Ranking.title(p.best), right, cy + 5);
        }
        g.dispose();
    }

    private static void drawRight(Graphics2D g, String text, int rightX, int y) {
        g.drawString(text, rightX - g.getFontMetrics().stringWidth(text), y);
    }
}
