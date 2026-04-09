package teams.team3_contacts;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MyContactsScreen extends JPanel {
    private final ContactsService contactsService;
    private final JPanel contactsListPanel;

    public MyContactsScreen(ContactsService contactsService) {
        this.contactsService = contactsService;
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        JLabel title = new JLabel("  My Contacts");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        title.setPreferredSize(new Dimension(0, 50));
        title.setOpaque(true);
        title.setBackground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        contactsListPanel = new JPanel();
        contactsListPanel.setLayout(new BoxLayout(contactsListPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(contactsListPanel);

        JScrollPane scroll = new JScrollPane(contactsListPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing MyContactsScreen after login for user: " + user.getId());
                refreshContacts();
            }
        });
    }

    public void refreshContacts() {
        System.out.println("[DEBUG] refreshContacts() called. Current user: " + SessionManager.getInstance().getCurrentUser());
        DebugLogger.info("MY_CONTACTS → refreshContacts() called.");
        SwingWorker<Void, User> worker = new SwingWorker<>() {
            List<User> contacts;
            @Override
            protected Void doInBackground() {
                DebugLogger.info("MY_CONTACTS → Fetching contacts from service...");
                contacts = contactsService.getContacts();
                System.out.println("[DEBUG] Contacts fetched: " + (contacts != null ? contacts.size() : 0) + " entries. Data: " + contacts);
                DebugLogger.info("MY_CONTACTS → Received " + (contacts != null ? contacts.size() : 0) + " contacts.");
                return null;
            }
            @Override
            protected void done() {
                DebugLogger.info("MY_CONTACTS → Updating UI with contacts list.");
                contactsListPanel.removeAll();
                if (contacts == null || contacts.isEmpty()) {
                    DebugLogger.info("MY_CONTACTS → No contacts found, showing empty label.");
                    contactsListPanel.add(createEmptyLabel("No contacts yet. Use search to find friends!"));
                } else {
                    DebugLogger.info("MY_CONTACTS → Creating cards for " + contacts.size() + " users.");
                    for (User u : contacts) {
                        contactsListPanel.add(createContactCard(u));
                    }
                }
                contactsListPanel.revalidate();
                contactsListPanel.repaint();
                DebugLogger.info("MY_CONTACTS → UI update complete.");
            }
        };
        worker.execute();
    }

    private JPanel createContactCard(User u) {
        DebugLogger.info("MY_CONTACTS → Creating card for User ID: " + u.getId() + " | Name: " + u.getFullName() + " | Email: " + u.getEmail());
        JPanel card = ContactCardFactory.createBaseCard(u);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton chatBtn = UIUtils.createRoundedButton("Chat", UIUtils.PRIMARY_COLOR, Color.WHITE);
        chatBtn.setPreferredSize(new Dimension(100, 40));
        chatBtn.addActionListener(e -> {
            DebugLogger.info("MY_CONTACTS → Chat button clicked for User ID: " + u.getId() + " (" + u.getFullName() + ")");
            contactsService.openChat(u.getId());
        });

        JButton removeBtn = UIUtils.createRoundedButton("Remove", Color.WHITE, Color.DARK_GRAY);
        removeBtn.setPreferredSize(new Dimension(100, 40));
        removeBtn.addActionListener(e -> {
            DebugLogger.info("MY_CONTACTS → Remove button clicked for User ID: " + u.getId() + " (" + u.getFullName() + ")");
            if (JOptionPane.showConfirmDialog(this, "Remove " + u.getFullName() + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DebugLogger.info("MY_CONTACTS → User confirmed removal of User ID: " + u.getId());
                boolean success = contactsService.deleteContact(u.getId());
                DebugLogger.info("MY_CONTACTS → Result of deleteContact(" + u.getId() + "): " + success);
                if (success) {
                    UIUtils.showToast("Contact removed", this);
                    refreshContacts();
                }
            }
        });

        JButton blockBtn = UIUtils.createRoundedButton("Block", Color.WHITE, Color.RED);
        blockBtn.setPreferredSize(new Dimension(100, 40));
        blockBtn.addActionListener(e -> {
            DebugLogger.info("MY_CONTACTS → Block button clicked for User ID: " + u.getId() + " (" + u.getFullName() + ")");
            if (JOptionPane.showConfirmDialog(this, "Block " + u.getFullName() + "?", "Confirm Block", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DebugLogger.info("MY_CONTACTS → User confirmed block of User ID: " + u.getId());
                boolean success = contactsService.blockUser(u.getId());
                DebugLogger.info("MY_CONTACTS → Result of blockUser(" + u.getId() + "): " + success);
                if (success) {
                    UIUtils.showToast("User blocked", this);
                    refreshContacts();
                }
            }
        });

        actions.add(chatBtn);
        actions.add(removeBtn);
        actions.add(blockBtn);
        card.add(actions, BorderLayout.EAST);
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
