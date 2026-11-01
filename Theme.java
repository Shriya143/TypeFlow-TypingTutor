package typingtutor;

import java.awt.Color;
import java.awt.Font;

/** Central place for colours and fonts so the whole app looks consistent. */
public final class Theme {
    private Theme() {}

    public static final Color BG     = new Color(0x0F172A);
    public static final Color CARD   = new Color(0x1E1B4B);
    public static final Color CARD_ALPHA = new Color(0x1E, 0x1B, 0x4B, 185);
    public static final Color YELLOW = new Color(0xFACC15);
    public static final Color PINK   = new Color(0xF472B6);
    public static final Color ORANGE = new Color(0xFB923C);
    public static final Color PURPLE = new Color(0x8B5CF6);
    public static final Color TEXT   = new Color(0xE2E8F0);
    public static final Color MUTED  = new Color(0xA5B4FC);
    public static final Color ACCENT = new Color(0x38BDF8);
    public static final Color GOOD   = new Color(0x4ADE80);
    public static final Color BAD    = new Color(0xF87171);

    public static final Font MONO  = new Font(Font.MONOSPACED, Font.PLAIN, 24);
    public static final Font UI    = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BOLD  = new Font("SansSerif", Font.BOLD, 14);
    public static final Font BIG   = new Font("SansSerif", Font.BOLD, 30);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 22);

    /** Blends colour a into colour b. f=0 gives a, f=1 gives b. */
    public static Color mix(Color a, Color b, double f) {
        f = Math.max(0, Math.min(1, f));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * f),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * f),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * f));
    }
}
