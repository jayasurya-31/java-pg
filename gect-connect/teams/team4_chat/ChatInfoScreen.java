package teams.team4_chat;

import shared.UIUtils;
import shared.SessionManager;
import shared.DebugLogger;
import core.User;
import javax.swing.*;
import java.awt.*;

/**
 * Chat Info Screen displaying contact profile details.
 */
public class ChatInfoScreen extends JPanel {
    private final User contact;
    private final ChatDAO chatDAO;

    public ChatInfoScreen(User contact, Runnable backAction) {
        this.contact = contact;
        this.chatDAO = new ChatDAO();
        
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        setBackground(Color.WHITE);

        // Header with Back Button
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        header.setOpaque(false);
        JButton backBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        backBtn.setIcon(shared.Icons.get("back"));
        backBtn.setPreferredSize(new Dimension(55, 45));
        backBtn.addActionListener(e -> backAction.run());
        header.add(backBtn);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Avatar
        JLabel avatar = new JLabel(shared.Icons.get("user_avatar"));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(avatar);

        // Name
        content.add(Box.createVerticalStrut(25));
        JLabel nameLabel = new JLabel(contact.getFullName(), JLabel.CENTER);
        nameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(nameLabel);

        // Status
        content.add(Box.createVerticalStrut(10));
        JLabel statusLabel = new JLabel(contact.getStatus() != null ? contact.getStatus() : "No status available", JLabel.CENTER);
        statusLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_NORMAL));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(statusLabel);

        // Stats (Shared Media/Files)
        content.add(Box.createVerticalStrut(40));
        User self = SessionManager.getInstance().getCurrentUser();
        if (self != null) {
            int mediaCount = chatDAO.getSharedMediaCount(self.getUserId(), contact.getUserId());
            int filesCount = chatDAO.getSharedFilesCount(self.getUserId(), contact.getUserId());
            
            content.add(createStatItem("Shared Media", mediaCount));
            content.add(Box.createVerticalStrut(15));
            content.add(createStatItem("Shared Files", filesCount));
        }

        // Actions
        content.add(Box.createVerticalStrut(40));
        JButton muteBtn = UIUtils.createRoundedButton("Mute Notifications", Color.decode("#f0f2f5"), Color.BLACK);
        muteBtn.setPreferredSize(new Dimension(300, UIUtils.BUTTON_HEIGHT));
        muteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        muteBtn.addActionListener(e -> {
            DebugLogger.info("CHAT ? MUTE_USER on " + contact.getEmail() + " - actionPerformed() - ChatInfoScreen.java");
            UIUtils.showToast("Notifications Muted", this);
        });
        content.add(muteBtn);
        
        content.add(Box.createVerticalStrut(15));
        JButton blockBtn = UIUtils.createRoundedButton("Block User", Color.decode("#fde8e8"), Color.RED);
        blockBtn.setPreferredSize(new Dimension(300, UIUtils.BUTTON_HEIGHT));
        blockBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        blockBtn.addActionListener(e -> {
            DebugLogger.info("CHAT ? BLOCK_USER on " + contact.getEmail() + " - actionPerformed() - ChatInfoScreen.java");
            int confirm = JOptionPane.showConfirmDialog(this, "Block " + contact.getFullName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (new teams.team3_contacts.ContactsService().blockUser(contact.getUserId())) {
                    UIUtils.showToast("User Blocked", this);
                    backAction.run();
                }
            }
        });
        content.add(blockBtn);

        add(new JScrollPane(content) {{ setBorder(null); setOpaque(false); getViewport().setOpaque(false); }}, BorderLayout.CENTER);
    }

    private JPanel createStatItem(String label, int count) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(500, 50));
        JLabel l = new JLabel(label);
        l.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        p.add(l, BorderLayout.WEST);
        
        JLabel c = new JLabel(String.valueOf(count), JLabel.RIGHT);
        c.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        p.add(c, BorderLayout.EAST);
        
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));
        return p;
    }
}
