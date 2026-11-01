package typingtutor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Analytics screen: summary cards, WPM progress line chart, and most-missed keys bar chart. */
public class DashboardPanel extends JPanel {
    private final String user;
    private final StatsRepository repo;
    private List<SessionResult> data = new ArrayList<>();

    private final JLabel bestLbl = new JLabel();
    private final JLabel avgLbl = new JLabel();
    private final JLabel accLbl = new JLabel();
    private final JLabel countLbl = new JLabel();
    private final ChartArea charts = new ChartArea();

    public DashboardPanel(String user, StatsRepository repo) {
        this.user = user;
        this.repo = repo;
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        for (JLabel l : new JLabel[]{bestLbl, avgLbl, accLbl, countLbl}) cards.add(card(l));
        add(cards, BorderLayout.NORTH);
        add(charts, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        data = repo.loadForUser(user);
        double best = 0, sumWpm = 0, sumAcc = 0;
        for (SessionResult r : data) {
            best = Math.max(best, r.getWpm());
            sumWpm += r.getWpm();
            sumAcc += r.getAccuracy();
        }
        int n = data.size();
        setCard(bestLbl, "Best WPM", String.format("%.0f", best));
        setCard(avgLbl, "Average WPM", n == 0 ? "0" : String.format("%.0f", sumWpm / n));
        setCard(accLbl, "Avg Accuracy", n == 0 ? "0%" : String.format("%.0f%%", sumAcc / n));
        setCard(countLbl, "Sessions", String.valueOf(n));
        charts.repaint();
    }

    private JPanel card(JLabel l) {
        RoundedPanel p = new RoundedPanel(new BorderLayout(), Theme.CARD_ALPHA, 24);
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        p.add(l);
        return p;
    }

    private void setCard(JLabel l, String name, String value) {
        String color = name.startsWith("Best") ? "#FACC15" : name.startsWith("Average") ? "#38BDF8"
                : name.startsWith("Avg") ? "#4ADE80" : "#F472B6";
        l.setText("<html><span style='color:#C4B5FD;font-size:11px'>" + name.toUpperCase()
                + "</span><br><span style='color:" + color + ";font-size:26px'><b>" + value + "</b></span></html>");
    }

    /** Custom-drawn charts using Java2D (no external libraries needed). */
    private class ChartArea extends JPanel {
        ChartArea() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setColor(Theme.CARD_ALPHA);
            g.fillRoundRect(0, 0, w, h, 28, 28);
            int split = (int) (w * 0.62);
            drawLineChart(g, 0, 0, split, h);
            drawBarChart(g, split, 0, w - split, h);
            g.dispose();
        }

        private void drawLineChart(Graphics2D g, int x0, int y0, int w, int h) {
            g.setFont(Theme.BOLD);
            g.setColor(Theme.TEXT);
            g.drawString("Speed progress (WPM, last 15 sessions)", x0 + 24, y0 + 30);
            int left = x0 + 56, right = x0 + w - 24, top = y0 + 52, bottom = y0 + h - 36;

            int from = Math.max(0, data.size() - 15);
            List<SessionResult> pts = data.subList(from, data.size());
            if (pts.isEmpty()) { drawEmpty(g, left, top, right - left, bottom - top); return; }

            double max = 10;
            for (SessionResult r : pts) max = Math.max(max, r.getWpm());
            max = Math.ceil(max * 1.2 / 10) * 10;

            g.setFont(Theme.UI);
            for (int i = 0; i <= 4; i++) {
                int y = bottom - (bottom - top) * i / 4;
                g.setColor(new Color(0x334155));
                g.drawLine(left, y, right, y);
                g.setColor(Theme.MUTED);
                g.drawString(String.valueOf((int) (max * i / 4)), left - 36, y + 5);
            }

            int n = pts.size();
            double[] px = new double[n], py = new double[n];
            for (int i = 0; i < n; i++) {
                px[i] = n == 1 ? (left + right) / 2.0 : left + (right - left) * i / (double) (n - 1);
                py[i] = bottom - (bottom - top) * (pts.get(i).getWpm() / max);
            }
            if (n > 1) {
                Path2D area = new Path2D.Double();
                area.moveTo(px[0], bottom);
                for (int i = 0; i < n; i++) area.lineTo(px[i], py[i]);
                area.lineTo(px[n - 1], bottom);
                area.closePath();
                g.setPaint(new GradientPaint(0, top, new Color(56, 189, 248, 90), 0, bottom, new Color(56, 189, 248, 0)));
                g.fill(area);

                g.setColor(Theme.ACCENT);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < n - 1; i++) g.drawLine((int) px[i], (int) py[i], (int) px[i + 1], (int) py[i + 1]);
            }
            for (int i = 0; i < n; i++) {
                g.setColor(Theme.CARD);
                g.fillOval((int) px[i] - 6, (int) py[i] - 6, 12, 12);
                g.setColor(Theme.ACCENT);
                g.setStroke(new BasicStroke(2f));
                g.drawOval((int) px[i] - 6, (int) py[i] - 6, 12, 12);
            }
            g.setColor(Theme.MUTED);
            g.drawString("oldest", left, bottom + 22);
            g.drawString("latest", right - 34, bottom + 22);
        }

        private void drawBarChart(Graphics2D g, int x0, int y0, int w, int h) {
            g.setStroke(new BasicStroke(1f));
            g.setFont(Theme.BOLD);
            g.setColor(Theme.TEXT);
            g.drawString("Most missed keys", x0 + 16, y0 + 30);
            int left = x0 + 16, right = x0 + w - 24, top = y0 + 52, bottom = y0 + h - 36;

            Map<Character, Integer> total = new HashMap<>();
            for (SessionResult r : data) r.getMistakes().forEach((k, v) -> total.merge(k, v, Integer::sum));
            List<Map.Entry<Character, Integer>> top6 = new ArrayList<>(total.entrySet());
            top6.sort((a, b) -> b.getValue() - a.getValue());
            if (top6.size() > 6) top6 = top6.subList(0, 6);
            if (top6.isEmpty()) { drawEmpty(g, left, top, right - left, bottom - top); return; }

            int max = top6.get(0).getValue();
            int rowH = (bottom - top) / top6.size();
            g.setFont(Theme.UI);
            for (int i = 0; i < top6.size(); i++) {
                Map.Entry<Character, Integer> e = top6.get(i);
                int y = top + i * rowH;
                String key = e.getKey() == ' ' ? "space" : String.valueOf(e.getKey());
                g.setColor(Theme.TEXT);
                g.drawString(key, left, y + rowH / 2 + 4);
                int barMax = right - left - 100;
                int len = Math.max(6, (int) (barMax * e.getValue() / (double) max));
                g.setColor(Theme.BAD);
                g.fillRoundRect(left + 56, y + rowH / 2 - 8, len, 16, 8, 8);
                g.setColor(Theme.MUTED);
                g.drawString(String.valueOf(e.getValue()), left + 62 + len, y + rowH / 2 + 4);
            }
        }

        private void drawEmpty(Graphics2D g, int x, int y, int w, int h) {
            g.setFont(Theme.UI);
            g.setColor(Theme.MUTED);
            String msg = "No data yet. Finish a session!";
            int tw = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, x + (w - tw) / 2, y + h / 2);
        }
    }
}
