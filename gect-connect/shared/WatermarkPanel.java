package shared;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A custom panel that renders a subtle watermark in the background.
 * Opacity: 5%, Grayscale, Centered.
 */
public class WatermarkPanel extends JPanel {
    private final ImageIcon watermarkIcon;
    private final float opacity = 0.05f;

    public WatermarkPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        this.watermarkIcon = Icons.get("watermark");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (watermarkIcon == null || watermarkIcon.getImage() == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Center placement
        int x = (getWidth() - watermarkIcon.getIconWidth()) / 2;
        int y = (getHeight() - watermarkIcon.getIconHeight()) / 2;

        // Apply opacity
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

        // Draw image (Icons.java already returns scaled image)
        g2.drawImage(watermarkIcon.getImage(), x, y, this);
        
        g2.dispose();
    }
}
