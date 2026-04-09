package teams.team3_contacts;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContactCardFactory {
    private static final Map<String, ImageIcon> imageCache = new ConcurrentHashMap<>();

    public static JPanel createBaseCard(User u) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setPreferredSize(new Dimension(0, 90));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel info = new JLabel(u.getFullName() + " | " + u.getEmail());
        info.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 16));

        ImageIcon icon = getProfileIcon(u.getProfilePic());
        info.setIcon(icon);
        info.setIconTextGap(15);

        card.add(info, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { card.setBackground(new Color(245,245,245)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    private static ImageIcon getProfileIcon(String path) {
        if (path != null && !path.isEmpty() && new java.io.File(path).exists()) {
            return imageCache.computeIfAbsent(path, p -> {
                ImageIcon icon = new ImageIcon(new ImageIcon(p).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                return icon;
            });
        } else {
            return (ImageIcon) Icons.get("user_avatar", 50);
        }
    }
}
