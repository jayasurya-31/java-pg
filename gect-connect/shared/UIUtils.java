package shared;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Common UI helper methods for modern Swing styling.
 */
public class UIUtils {
    public static final Color PRIMARY_COLOR = new Color(29, 185, 84); // #1DB954
    public static final Color SECONDARY_COLOR = new Color(0, 150, 80);
    public static final Color DARK_PRIMARY = new Color(0, 122, 67);
    public static final Color LIGHT_PRIMARY = new Color(230, 255, 240);
    public static final Color LIGHT_BLUE = new Color(185, 223, 255); // #B9DFFF
    public static final Color ACCENT_COLOR = new Color(37, 211, 102);
    public static final Color SENT_BUBBLE_COLOR = new Color(0, 150, 80);
    public static final Color RECEIVED_BUBBLE_COLOR = new Color(241, 241, 241);
    public static final Color BACKGROUND_COLOR = Color.WHITE; // #FFFFFF
    public static final Color SIDEBAR_BG = Color.WHITE;
    public static final Color NAVBAR_BG = new Color(0, 150, 80, 217); // Glass effect transparency 0.85
    public static final Color TEXT_PRIMARY = new Color(26, 26, 26);
    public static final Color TEXT_SECONDARY = new Color(119, 119, 119);
    public static final Color BORDER_COLOR = new Color(224, 224, 224); // #E0E0E0
    public static final Color HOVER_COLOR = new Color(230, 255, 240);
    public static final Color ACTIVE_COLOR = new Color(29, 185, 84);
    public static final Color ACTIVE_TEXT = Color.WHITE;
    public static final Color NOTIF_RED = new Color(255, 59, 48);

    // Neumorphism colors
    public static final Color NEUMORPHIC_BG = new Color(240, 245, 242);
    public static final Color NEUMORPHIC_LIGHT = Color.WHITE;
    public static final Color NEUMORPHIC_DARK = new Color(200, 208, 204);

    // Scaling & Typography Profile 2.0
    public static final String FONT_FAMILY = "Roboto";
    public static final double SCALE_FACTOR = 1.3;

    public static final int HEADING_XL = 36;
    public static final int HEADING_L = 30;
    public static final int HEADING_M = 24;
    public static final int BODY_LARGE = 20;
    public static final int BODY_NORMAL = 18;
    public static final int BODY_SMALL = 16;
    public static final int CAPTION = 14;

    public static final int INPUT_HEIGHT = 55;
    public static final int INPUT_FONT_SIZE = 20;
    public static final int INPUT_PADDING_H = 14;
    public static final int INPUT_BORDER_RADIUS = 12;
    public static final int PLACEHOLDER_FONT_SIZE = 18;

    public static final int BUTTON_HEIGHT = 55;
    public static final int BUTTON_FONT_SIZE = 18;
    public static final int BUTTON_CORNER_RADIUS = 12;
    public static final int BUTTON_MIN_WIDTH = 160;

    public static final int BASE_UNIT = 10;
    public static final int SECTION_GAP = 24;
    public static final int COMPONENT_GAP = 16;

    public static final Border DEFAULT_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
        BorderFactory.createEmptyBorder(5, INPUT_PADDING_H, 5, INPUT_PADDING_H)
    );

    private static boolean isDarkMode = false;

    public static void stylePanel(JPanel panel) {
        panel.setBackground(getPanelColor());
    }

    public static JButton createRoundedButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), BUTTON_CORNER_RADIUS, BUTTON_CORNER_RADIUS);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font(FONT_FAMILY, Font.BOLD, BUTTON_FONT_SIZE));
        button.setPreferredSize(new Dimension(BUTTON_MIN_WIDTH, BUTTON_HEIGHT));
        return button;
    }

    public static JTextField createModernTextField(String placeholder) {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && ! (FocusManager.getCurrentManager().getFocusOwner() == this)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font(FONT_FAMILY, Font.ITALIC, PLACEHOLDER_FONT_SIZE));
                    g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getAscent() + getInsets().top);
                    g2.dispose();
                }
            }
        };
        textField.setFont(new Font(FONT_FAMILY, Font.PLAIN, INPUT_FONT_SIZE));
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, INPUT_HEIGHT));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(5, INPUT_PADDING_H, 5, INPUT_PADDING_H)
        ));
        return textField;
    }

    public static JPasswordField createModernPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && ! (FocusManager.getCurrentManager().getFocusOwner() == this)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font(FONT_FAMILY, Font.ITALIC, PLACEHOLDER_FONT_SIZE));
                    g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getAscent() + getInsets().top);
                    g2.dispose();
                }
            }
        };
        passwordField.setFont(new Font(FONT_FAMILY, Font.PLAIN, INPUT_FONT_SIZE));
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, INPUT_HEIGHT));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(5, INPUT_PADDING_H, 5, INPUT_PADDING_H)
        ));
        return passwordField;
    }

    public static void showToast(String message, JComponent parent) {
        JWindow toast = new JWindow();
        toast.setLayout(new BorderLayout());
        toast.setSize(300, 50);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label, BorderLayout.CENTER);
        
        toast.add(panel);
        
        try {
            Point location = parent.getLocationOnScreen();
            toast.setLocation(location.x + (parent.getWidth() - 300) / 2, location.y + 50);
        } catch (Exception e) {
            toast.setLocationRelativeTo(null);
        }
        
        toast.setAlwaysOnTop(true);
        toast.setVisible(true);
        
        new Timer(3000, e -> {
            toast.dispose();
            ((Timer)e.getSource()).stop();
        }).start();
    }

    public static JDialog showLoadingDialog(Component parent, String message) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Loading", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(250, 80);
        dialog.setLocationRelativeTo(parent);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label, BorderLayout.CENTER);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setForeground(PRIMARY_COLOR);
        bar.setBorderPainted(false);
        panel.add(bar, BorderLayout.SOUTH);

        dialog.add(panel);
        
        // Ensure it doesn't block forever in case it's not disposed properly
        new Timer(10000, e -> {
            if (dialog.isVisible()) dialog.dispose();
            ((Timer)e.getSource()).stop();
        }).start();

        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        return dialog;
    }

    public static class FadePanel extends JPanel {
        private float alpha = 1.0f;

        public FadePanel() {
            setOpaque(false);
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
            g2.dispose();
        }
    }

    public static void toggleDarkMode(boolean dark) {
        isDarkMode = dark;
        EventBus.getInstance().publish("THEME_CHANGED", dark);
    }

    public static Color getBackgroundColor() {
        return isDarkMode ? new Color(30, 30, 30) : BACKGROUND_COLOR;
    }

    public static Color getTextColor() {
        return isDarkMode ? Color.WHITE : TEXT_PRIMARY;
    }

    public static Color getSecondaryTextColor() {
        return isDarkMode ? new Color(180, 180, 180) : TEXT_SECONDARY;
    }

    public static Color getPanelColor() {
        return isDarkMode ? new Color(45, 45, 45) : Color.WHITE;
    }

    public static Color getSidebarColor() {
        return isDarkMode ? new Color(35, 35, 35) : SIDEBAR_BG;
    }

    public static Color getNavbarColor() {
        return isDarkMode ? NAVBAR_BG.darker() : NAVBAR_BG;
    }

    public static Color getBorderColor() {
        return isDarkMode ? new Color(60, 60, 60) : BORDER_COLOR;
    }

    public static void setModernLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Custom ScrollBar UI
            UIManager.put("ScrollBar.thumb", BORDER_COLOR);
            UIManager.put("ScrollBar.thumbDarkShadow", BORDER_COLOR);
            UIManager.put("ScrollBar.thumbHighlight", BORDER_COLOR);
            UIManager.put("ScrollBar.thumbShadow", BORDER_COLOR);
            UIManager.put("ScrollBar.track", Color.WHITE);
            UIManager.put("ScrollBar.width", 6);
            UIManager.put("ScrollBar.thumbArc", 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyFloatingCardStyle(JPanel panel) {
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            new Border() {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(x + 2, y + 2, width - 4, height - 4, 14, 14);
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(x, y, width - 1, height - 1, 14, 14);
                    g2.dispose();
                }
                @Override
                public Insets getBorderInsets(Component c) { return new Insets(10, 10, 10, 10); }
                @Override
                public boolean isBorderOpaque() { return false; }
            }
        ));
    }

    /**
     * Applies a subtle background watermark to a panel.
     */
    public static JPanel wrapWithWatermark(JPanel content, LayoutManager layout) {
        WatermarkPanel watermarkPanel = new WatermarkPanel(layout);
        watermarkPanel.add(content);
        return watermarkPanel;
    }

    public static JPanel createGlassPanel(int radius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background with transparency (Glass effect)
                g2.setColor(new Color(255, 255, 255, 64));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                
                // Border with transparency
                g2.setColor(new Color(255, 255, 255, 77));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel createNeumorphicPanel(int radius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                // Dark shadow (bottom right)
                g2.setColor(NEUMORPHIC_DARK);
                g2.fillRoundRect(4, 4, w - 4, h - 4, radius, radius);
                
                // Light shadow (top left)
                g2.setColor(NEUMORPHIC_LIGHT);
                g2.fillRoundRect(0, 0, w - 4, h - 4, radius, radius);
                
                // Main background
                g2.setColor(NEUMORPHIC_BG);
                g2.fillRoundRect(2, 2, w - 4, h - 4, radius, radius);
                
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static void applyHoverLift(JComponent component) {
        component.addMouseListener(new java.awt.event.MouseAdapter() {
            private Point originalLocation;
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                originalLocation = component.getLocation();
                component.setLocation(originalLocation.x, originalLocation.y - 2);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (originalLocation != null) {
                    component.setLocation(originalLocation);
                }
            }
        });
    }

    public static JLabel createIconLabel(String iconName, int size, String fallback) {
        JLabel label = new JLabel();
        ImageIcon icon = Icons.get(iconName);
        if (icon != null && icon.getIconWidth() > 0) {
            label.setIcon(icon);
        } else {
            // Only use fallback if icon is not found in Icons.java
            label.setText(fallback);
            label.setFont(new Font("Segoe UI", Font.PLAIN, size));
        }
        return label;
    }

    public static class ValidationUtils {
        public static boolean isValidEmail(String email) {
            return email != null && email.matches("^[a-zA-Z0-9._%+-]+@gectcr\\.ac\\.in$");
        }

        public static boolean isValidPassword(String password) {
            return password != null && password.length() >= 6;
        }

        public static String trim(String text) {
            return text == null ? "" : text.trim();
        }
    }
}
