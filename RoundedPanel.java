package typingtutor;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/** A panel with rounded corners and a see-through colour. */
public class RoundedPanel extends JPanel {
    private final Color fill;
    private final int arc;

    public RoundedPanel(LayoutManager layout, Color fill, int arc) {
        super(layout);
        this.fill = fill;
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(fill);
        g.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g.dispose();
    }
}
