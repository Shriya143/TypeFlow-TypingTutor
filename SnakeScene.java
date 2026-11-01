package typingtutor;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/** Sentences mode: a snake eats an apple for every correct word and grows longer. */
public class SnakeScene extends JPanel {
    private int words;
    private double dispLen = 3;
    private float pop;
    private float t;
    private int lastCap = 200;

    public SnakeScene() {
        setOpaque(false);
        new Timer(33, e -> {
            t += 0.033f;
            double target = Math.min(lastCap, 3 + 2 * words);
            dispLen += (target - dispLen) * 0.25;
            if (Math.abs(target - dispLen) < 0.05) dispLen = target;
            if (pop > 0) pop -= 0.03f;
            repaint();
        }).start();
    }

    public void setProgress(int w) {
        if (w > words) pop = 1f;
        words = w;
    }

    public void reset() {
        words = 0;
        dispLen = 3;
        pop = 0;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setClip(new RoundRectangle2D.Double(0, 0, w, h, 28, 28));
        g.setPaint(new GradientPaint(0, 0, new Color(0x065F46), 0, h, new Color(0x022C22)));
        g.fillRect(0, 0, w, h);

        int cell = 30, hud = 44;
        int cols = Math.max(4, (w - 30) / cell);
        int rows = Math.max(2, (h - hud - 16) / cell);
        int padX = (w - cols * cell) / 2;
        int padY = hud + (h - hud - rows * cell) / 2;
        int total = cols * rows;
        lastCap = total - 1;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if ((r + c) % 2 == 0) {
                    g.setColor(new Color(255, 255, 255, 14));
                    g.fillRect(padX + c * cell, padY + r * cell, cell, cell);
                }
            }
        }

        int n = Math.min((int) Math.round(dispLen), total - 1);
        Color tail = new Color(0x15803D), head = new Color(0x4ADE80);
        g.setStroke(new BasicStroke(cell * 0.78f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < n - 1; i++) {
            double[] a = pos(i, cols, padX, padY, cell), b = pos(i + 1, cols, padX, padY, cell);
            g.setColor(Theme.mix(tail, head, i / (double) Math.max(1, n - 1)));
            g.drawLine((int) a[0], (int) a[1], (int) b[0], (int) b[1]);
        }
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < n - 1; i += 2) {
            double[] a = pos(i, cols, padX, padY, cell);
            g.setColor(new Color(255, 255, 255, 60));
            g.fill(new Ellipse2D.Double(a[0] - cell * 0.15, a[1] - cell * 0.15, cell * 0.3, cell * 0.3));
        }

        double[] hp = pos(n - 1, cols, padX, padY, cell);
        int dir = ((n - 1) / cols) % 2 == 0 ? 1 : -1;
        g.setColor(head);
        g.fill(new Ellipse2D.Double(hp[0] - cell * 0.54, hp[1] - cell * 0.54, cell * 1.08, cell * 1.08));
        for (int s = -1; s <= 1; s += 2) {
            double ex = hp[0] + dir * cell * 0.12, ey = hp[1] + s * cell * 0.22;
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(ex - cell * 0.15, ey - cell * 0.15, cell * 0.3, cell * 0.3));
            g.setColor(Color.BLACK);
            g.fill(new Ellipse2D.Double(ex + dir * cell * 0.04 - cell * 0.07, ey - cell * 0.07, cell * 0.14, cell * 0.14));
        }
        if (Math.sin(t * 7) > 0) {
            g.setColor(new Color(0xEF4444));
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            double x1 = hp[0] + dir * cell * 0.5, x2 = hp[0] + dir * cell * 0.95;
            g.drawLine((int) x1, (int) hp[1], (int) x2, (int) hp[1]);
            g.drawLine((int) x2, (int) hp[1], (int) (x2 + dir * 5), (int) hp[1] - 5);
            g.drawLine((int) x2, (int) hp[1], (int) (x2 + dir * 5), (int) hp[1] + 5);
        }

        if (n < total) {
            double[] ap = pos(n, cols, padX, padY, cell);
            double sc = 1 + 0.08 * Math.sin(t * 6);
            double rad = cell * 0.38 * sc;
            g.setColor(new Color(0xEF4444));
            g.fill(new Ellipse2D.Double(ap[0] - rad, ap[1] - rad + 2, rad * 2, rad * 2));
            g.setColor(new Color(255, 255, 255, 120));
            g.fill(new Ellipse2D.Double(ap[0] - rad * 0.5, ap[1] - rad * 0.6, rad * 0.5, rad * 0.5));
            g.setColor(new Color(0x86EFAC));
            g.fill(new Ellipse2D.Double(ap[0] + 1, ap[1] - rad - 5, 8, 5));
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Snake length: " + n, 18, 30);
        g.setFont(Theme.BOLD);
        g.setColor(Theme.YELLOW);
        String r = "Apples eaten: " + words;
        g.drawString(r, w - 18 - g.getFontMetrics().stringWidth(r), 30);

        if (pop > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.setColor(new Color(250, 204, 21, (int) (255 * Math.min(1, pop))));
            g.drawString("Yum! +1", (int) hp[0] - 20, (int) (hp[1] - 28 - (1 - pop) * 30));
        }
        g.dispose();
    }

    /** Position of the i-th cell along a zig-zag path (left to right, then right to left...). */
    private static double[] pos(int i, int cols, int padX, int padY, int cell) {
        int row = i / cols;
        int col = i % cols;
        if (row % 2 == 1) col = cols - 1 - col;
        return new double[]{padX + col * cell + cell / 2.0, padY + row * cell + cell / 2.0};
    }
}
