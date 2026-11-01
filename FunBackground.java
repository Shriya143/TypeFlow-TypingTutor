package typingtutor;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Colourful animated background with floating "stickers" (stars, hearts, keycaps...). */
public class FunBackground extends JPanel {
    private static class Sticker {
        int type;
        float x, y, size, rot, spin, phase, speed;
        Color color;
        char letter;
    }

    private final List<Sticker> stickers = new ArrayList<>();
    private float t = 0;

    public FunBackground() {
        setLayout(new BorderLayout());
        Color[] palette = {Theme.YELLOW, Theme.PINK, Theme.ACCENT, Theme.GOOD, Theme.ORANGE,
                new Color(0xC084FC), Color.WHITE};
        String letters = "TYPEFLOWKEYS";
        Random r = new Random(11);
        for (int i = 0; i < 32; i++) {
            Sticker s = new Sticker();
            s.type = i % 6;
            s.x = r.nextFloat();
            s.y = r.nextFloat();
            s.size = 22 + r.nextInt(30);
            s.rot = r.nextFloat() * 6.28f;
            s.spin = (r.nextFloat() - 0.5f) * 0.6f;
            s.phase = r.nextFloat() * 6.28f;
            s.speed = 0.6f + r.nextFloat();
            s.color = palette[r.nextInt(palette.length)];
            s.letter = letters.charAt(r.nextInt(letters.length()));
            stickers.add(s);
        }
        new Timer(40, e -> { t += 0.04f; repaint(); }).start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g.setPaint(new LinearGradientPaint(0, 0, w, h, new float[]{0f, 0.45f, 0.8f, 1f},
                new Color[]{new Color(0x312E81), new Color(0x7E22CE), new Color(0xDB2777), new Color(0xF97316)}));
        g.fillRect(0, 0, w, h);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        for (Sticker s : stickers) {
            Graphics2D sg = (Graphics2D) g.create();
            double px = s.x * w;
            double py = s.y * h + Math.sin(t * s.speed + s.phase) * 16;
            sg.translate(px, py);
            sg.rotate(s.rot + t * s.spin);
            drawSticker(sg, s);
            sg.dispose();
        }
        g.dispose();
    }

    private void drawSticker(Graphics2D g, Sticker s) {
        double r = s.size / 2.0;
        g.setColor(s.color);
        switch (s.type) {
            case 0: g.fill(star(0, 0, r)); break;
            case 1:
                g.fillOval((int) -r, (int) -r, (int) s.size, (int) s.size);
                g.setColor(new Color(255, 255, 255, 120));
                g.fillOval((int) (-r * 0.5), (int) (-r * 0.6), (int) (r * 0.5), (int) (r * 0.5));
                break;
            case 2:
                Path2D heart = new Path2D.Double();
                heart.moveTo(0, 0.9 * r);
                heart.curveTo(-1.3 * r, 0.1 * r, -0.8 * r, -r, 0, -0.35 * r);
                heart.curveTo(0.8 * r, -r, 1.3 * r, 0.1 * r, 0, 0.9 * r);
                g.fill(heart);
                break;
            case 3:
                g.setColor(new Color(0, 0, 0, 70));
                g.fill(new RoundRectangle2D.Double(-r + 3, -r + 5, s.size, s.size, 10, 10));
                g.setColor(Color.WHITE);
                g.fill(new RoundRectangle2D.Double(-r, -r, s.size, s.size, 10, 10));
                g.setColor(s.color == Color.WHITE ? Theme.PURPLE : s.color.darker());
                g.setFont(new Font("SansSerif", Font.BOLD, (int) (s.size * 0.6)));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(String.valueOf(s.letter), -fm.charWidth(s.letter) / 2f, fm.getAscent() / 2f - 2);
                break;
            case 4:
                g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                Path2D wave = new Path2D.Double();
                wave.moveTo(-r * 1.4, 0);
                for (int i = 1; i <= 20; i++) {
                    double xx = -r * 1.4 + i * r * 2.8 / 20;
                    wave.lineTo(xx, Math.sin(i * 0.9) * r * 0.45);
                }
                g.draw(wave);
                break;
            default:
                g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine((int) -r, 0, (int) r, 0);
                g.drawLine(0, (int) -r, 0, (int) r);
        }
    }

    /** 5-pointed star shape. */
    public static Path2D star(double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double rad = (i % 2 == 0) ? r : r * 0.45;
            double x = cx + Math.cos(ang) * rad, y = cy + Math.sin(ang) * rad;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }
}
