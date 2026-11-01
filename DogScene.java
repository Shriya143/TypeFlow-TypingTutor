package typingtutor;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

/** Words mode: a dog gets a balloon for every correct word and floats higher and higher. */
public class DogScene extends JPanel {
    private static final Color[] BALLOONS = {
        new Color(0xEF4444), new Color(0xFACC15), new Color(0x38BDF8), new Color(0x4ADE80),
        new Color(0xF472B6), new Color(0xFB923C), new Color(0xA78BFA)
    };

    private int words;
    private double dispAlt;       // altitude currently shown (eases toward the real value)
    private float pop;
    private float t;
    private final double[][] clouds = new double[7][3];
    private final double[][] stars = new double[70][3];

    public DogScene() {
        setOpaque(false);
        Random r = new Random(5);
        for (double[] c : clouds) { c[0] = r.nextDouble(); c[1] = r.nextDouble(); c[2] = r.nextDouble(); }
        for (double[] s : stars) { s[0] = r.nextDouble(); s[1] = r.nextDouble(); s[2] = r.nextDouble(); }
        new Timer(33, e -> {
            t += 0.033f;
            double target = words * 10.0;
            dispAlt += (target - dispAlt) * 0.07;
            if (Math.abs(target - dispAlt) < 0.05) dispAlt = target;
            if (pop > 0) pop -= 0.025f;
            repaint();
        }).start();
    }

    public void setProgress(int w) {
        if (w > words) pop = 1f;
        words = w;
    }

    public void reset() {
        words = 0;
        dispAlt = 0;
        pop = 0;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setClip(new RoundRectangle2D.Double(0, 0, w, h, 28, 28));

        double f = Math.min(1, dispAlt / 500.0);
        Color top = Theme.mix(new Color(0x3B82F6), new Color(0x0B1026), f);
        Color bot = Theme.mix(new Color(0xBAE6FD), new Color(0x4C1D95), f);
        g.setPaint(new GradientPaint(0, 0, top, 0, h, bot));
        g.fillRect(0, 0, w, h);

        if (f > 0.15) {
            for (double[] s : stars) {
                double a = Math.min(1, (f - 0.15) * 1.6) * (0.6 + 0.4 * Math.sin(t * 2 + s[2] * 10));
                g.setColor(new Color(255, 255, 255, (int) Math.max(0, Math.min(255, 255 * a))));
                double sz = 2 + s[2] * 2.5;
                g.fill(new Ellipse2D.Double(s[0] * w, s[1] * h, sz, sz));
            }
        }
        if (f > 0.4) {
            g.setColor(new Color(0xFEF9C3));
            g.fillOval(w - 120, 50, 50, 50);
            g.setColor(Theme.mix(top, bot, 0.2));
            g.fillOval(w - 108, 44, 46, 46);
        }

        double cam = dispAlt * 6;
        int cloudAlpha = (int) (215 * (1 - f * 0.75));
        for (double[] c : clouds) {
            double s = 0.8 + c[2] * 0.9;
            double cy = ((c[1] * h + cam * (0.4 + c[2] * 0.6)) % (h + 140)) - 70;
            double cx = c[0] * w + Math.sin(t * 0.4 + c[1] * 10) * 12;
            g.setColor(new Color(255, 255, 255, cloudAlpha));
            g.fill(new Ellipse2D.Double(cx, cy + 10 * s, 70 * s, 26 * s));
            g.fill(new Ellipse2D.Double(cx + 18 * s, cy, 40 * s, 32 * s));
            g.fill(new Ellipse2D.Double(cx + 40 * s, cy + 8 * s, 50 * s, 28 * s));
        }

        double groundTop = h - 55 + cam;
        if (groundTop < h + 20) {
            g.setColor(new Color(0x16A34A));
            g.fill(new Ellipse2D.Double(-w * 0.3, groundTop, w * 1.6, h));
            g.setColor(new Color(0x15803D));
            g.fill(new Ellipse2D.Double(-w * 0.1, groundTop + 14, w * 1.2, h));
        }

        double groundDogY = h - 92, centerY = h * 0.64;
        double k = Math.min(1, dispAlt / 60.0);
        double dogX = w / 2.0 + Math.sin(t * 0.9) * 18;
        double dogY = groundDogY + (centerY - groundDogY) * k + (words > 0 ? Math.sin(t * 2) * 4 : 0);

        int b = Math.min(12, words);
        double ax = dogX - 6, ay = dogY - 22;
        for (int i = 0; i < b; i++) {
            double frac = b == 1 ? 0 : (i / (double) (b - 1) - 0.5) * 2;
            double ang = -Math.PI / 2 + frac * 0.9;
            double len = 95 + (i % 3) * 16;
            double bx = ax + Math.cos(ang) * len + Math.sin(t * 1.3 + i) * 5;
            double by = ay + Math.sin(ang) * len + Math.cos(t * 1.7 + i) * 4;
            g.setColor(new Color(255, 255, 255, 150));
            g.setStroke(new BasicStroke(1.4f));
            g.drawLine((int) ax, (int) ay, (int) bx, (int) (by + 19));
            Color c = BALLOONS[i % BALLOONS.length];
            g.setColor(c);
            g.fill(new Ellipse2D.Double(bx - 15, by - 19, 30, 38));
            g.setColor(new Color(255, 255, 255, 110));
            g.fill(new Ellipse2D.Double(bx - 9, by - 14, 8, 12));
        }

        drawDog(g, dogX, dogY, words > 0);

        g.setStroke(new BasicStroke(1f));
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        shadowText(g, "Altitude: " + (int) dispAlt + " m", 18, 30, Color.WHITE);
        g.setFont(Theme.BOLD);
        shadowText(g, zone(dispAlt), 18, 52, Theme.YELLOW);
        String bl = "Balloons: " + b;
        shadowText(g, bl, w - 18 - g.getFontMetrics().stringWidth(bl), 30, Color.WHITE);

        if (pop > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 26));
            g.setColor(new Color(250, 204, 21, (int) (255 * Math.min(1, pop))));
            g.drawString("+10 m", (int) dogX + 60, (int) (dogY - 70 - (1 - pop) * 50));
        }
        g.dispose();
    }

    private static String zone(double alt) {
        if (alt < 30) return "Zone: Backyard";
        if (alt < 100) return "Zone: Treetops";
        if (alt < 200) return "Zone: Cloud Land";
        if (alt < 350) return "Zone: Stratosphere";
        return "Zone: Outer Space";
    }

    private static void shadowText(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(new Color(0, 0, 0, 110));
        g.drawString(s, x + 1, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    private void drawDog(Graphics2D g0, double x, double y, boolean air) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        Color fur = new Color(0xE8B86D), dark = new Color(0xB9823F), ear = new Color(0x8B5A2B);

        Graphics2D tg = (Graphics2D) g.create();
        tg.translate(-30, -8);
        tg.rotate(0.8 + Math.sin(t * 9) * 0.4);
        tg.setColor(fur);
        tg.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        tg.drawLine(0, 0, -26, 0);
        tg.dispose();

        double[] lx = {-26, -12, 8, 22};
        for (int i = 0; i < 4; i++) {
            Graphics2D lg = (Graphics2D) g.create();
            lg.translate(lx[i], 14);
            if (air) lg.rotate(Math.sin(t * 3 + i * 1.7) * 0.3);
            lg.setColor(i % 2 == 0 ? dark : fur);
            lg.fillRoundRect(-4, 0, 9, 20, 6, 6);
            lg.fillOval(-6, 15, 13, 8);
            lg.dispose();
        }

        g.setColor(fur);
        g.fill(new Ellipse2D.Double(-34, -20, 68, 40));
        g.setColor(new Color(0xF5DEB3));
        g.fill(new Ellipse2D.Double(-20, 2, 40, 16));
        g.setColor(new Color(0xDC2626));
        g.fillRoundRect(14, -26, 9, 28, 5, 5);
        g.setColor(Theme.YELLOW);
        g.fillOval(15, -2, 8, 8);

        g.setColor(fur);
        g.fill(new Ellipse2D.Double(11, -47, 38, 38));
        Graphics2D eg = (Graphics2D) g.create();
        eg.translate(20, -44);
        eg.rotate(0.35);
        eg.setColor(ear);
        eg.fill(new Ellipse2D.Double(-6, -4, 13, 26));
        eg.dispose();
        g.setColor(new Color(0xF5DEB3));
        g.fill(new Ellipse2D.Double(38, -26, 24, 16));
        g.setColor(Color.BLACK);
        g.fill(new Ellipse2D.Double(56, -27, 9, 8));
        g.fill(new Ellipse2D.Double(35, -37, 7, 7));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(37, -36, 2.5, 2.5));
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1.8f));
        g.drawArc(46, -23, 12, 9, 200, 140);
        if (air) {
            g.setColor(new Color(0xFB7185));
            g.fill(new Ellipse2D.Double(50, -16, 7, 10));
        }
        g.dispose();
    }
}
