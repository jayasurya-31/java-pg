package teams.team3_contacts;
import teams.team3_contacts.ContactCardFactory;
import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * GUI screen for displaying blocked users.
 * Refactored to use ContactCardFactory.
 */
public class BlockedScreen extends JPanel {
    private final ContactsService contactsService;
    private final JPanel blockedUsersPanel;

    public BlockedScreen(ContactsService contactsService) {
        this.contactsService = contactsService;
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        JLabel title = new JLabel("  Blocked Users");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        title.setPreferredSize(new Dimension(0, 50));
        title.setOpaque(true);
        title.setBackground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        blockedUsersPanel = new JPanel();
        blockedUsersPanel.setLayout(new BoxLayout(blockedUsersPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(blockedUsersPanel);

        JScrollPane scroll = new JScrollPane(blockedUsersPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing BlockedScreen after login for user: " + user.getId());
                refreshBlocked();
            }
        });
    }

    public void refreshBlocked() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                DebugLogger.info("BLOCKED → Fetching blocked users...");
                return contactsService.getBlockedUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> blocked = get();
                    DebugLogger.info("BLOCKED → Found " + blocked.size() + " blocked users.");
                    blockedUsersPanel.removeAll();
                    if (blocked.isEmpty()) {
                        blockedUsersPanel.add(createEmptyLabel("You haven't blocked anyone"));
                    } else {
                        for (User u : blocked) {
                            blockedUsersPanel.add(createBlockedCard(u));
                        }
                    }
                    blockedUsersPanel.revalidate();
                    blockedUsersPanel.repaint();
                } catch (Exception e) {
                    DebugLogger.error("BLOCKED → Error refreshing blocked list: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel createBlockedCard(User u) {
        JPanel card = ContactCardFactory.createBaseCard(u);
        JButton unblockBtn = UIUtils.createRoundedButton("Unblock", UIUtils.PRIMARY_COLOR, Color.WHITE);
        unblockBtn.setPreferredSize(new Dimension(120, 40));
        unblockBtn.addActionListener(e -> {
            DebugLogger.info("BLOCKED → Unblock clicked for User ID: " + u.getId());
            if (contactsService.unblockUser(u.getId())) {
                UIUtils.showToast("User unblocked", this);
                refreshBlocked();
            }
        });
        card.add(unblockBtn, BorderLayout.EAST);
        return card;
    }

    private JLabel createEmptyLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, 16));
        label.setForeground(Color.GRAY);
        label.setPreferredSize(new Dimension(0, 100));
        return label;
    }
}
