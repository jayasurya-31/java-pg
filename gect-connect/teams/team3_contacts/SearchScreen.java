
package teams.team3_contacts;
import teams.team3_contacts.ContactCardFactory;
import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Screen for searching users and sending contact requests.
 * Refactored to use ContactCardFactory and added Block functionality.
 */
public class SearchScreen extends JPanel {
    private final ContactsService contactsService;
    private final JPanel resultsPanel;
    private final JTextField searchField;

    public SearchScreen(ContactsService contactsService) {
        this.contactsService = contactsService;
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        // Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setBackground(Color.WHITE);
        searchBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        searchField = UIUtils.createModernTextField("Search by name, email or department...");
        searchField.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));

        JButton searchBtn = UIUtils.createRoundedButton("Search", UIUtils.PRIMARY_COLOR, Color.WHITE);
        searchBtn.setPreferredSize(new Dimension(120, 45));
        searchBtn.addActionListener(e -> refresh());

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(searchBtn, BorderLayout.EAST);
        add(searchBar, BorderLayout.NORTH);

        // Results
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(resultsPanel);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing SearchScreen after login for user: " + user.getId());
                refresh();
            }
        });
    }

    public void refresh() {
        String query = searchField.getText().trim();
        if (query.equals("Search by name, email or department...")) query = "";

        final String finalQuery = query;
        DebugLogger.info("SEARCH → refresh() called with query: " + finalQuery);

        SwingWorker<List<UserRelationship>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserRelationship> doInBackground() {
                return contactsService.getUsersWithStatus(finalQuery);
            }

            @Override
            protected void done() {
                try {
                    List<UserRelationship> results = get();
                    DebugLogger.info("SEARCH → Found " + results.size() + " results.");
                    resultsPanel.removeAll();
                    
                    if (results.isEmpty()) {
                        resultsPanel.add(createEmptyLabel("No users found matching your search."));
                    } else {
                        for (UserRelationship ur : results) {
                            resultsPanel.add(createSearchCard(ur));
                        }
                    }
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                } catch (Exception e) {
                    DebugLogger.error("SEARCH → Error fetching results: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel createSearchCard(UserRelationship ur) {
        User u = ur.getUser();
        JPanel card = ContactCardFactory.createBaseCard(u);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        String status = ur.getRequestStatus();
        
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            JLabel label = new JLabel("Contact");
            label.setForeground(UIUtils.PRIMARY_COLOR);
            label.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 14));
            actions.add(label);
        } else if ("PENDING".equalsIgnoreCase(status)) {
            JLabel label = new JLabel("Request Sent");
            label.setForeground(Color.GRAY);
            label.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, 14));
            actions.add(label);
        } else {
            JButton addBtn = UIUtils.createRoundedButton("Add", UIUtils.PRIMARY_COLOR, Color.WHITE);
            addBtn.setPreferredSize(new Dimension(100, 40));
            addBtn.addActionListener(e -> {
                if (contactsService.sendRequest(u.getId())) {
                    UIUtils.showToast("Request Sent!", this);
                    refresh();
                }
            });
            actions.add(addBtn);
        }

        // Add Block button for everyone in search
        JButton blockBtn = UIUtils.createRoundedButton("Block", Color.WHITE, Color.RED);
        blockBtn.setPreferredSize(new Dimension(100, 40));
        blockBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Block " + u.getFullName() + "?", "Confirm Block", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                DebugLogger.info("SEARCH → Blocking user: " + u.getId());
                if (contactsService.blockUser(u.getId())) {
                    UIUtils.showToast("User Blocked", this);
                    refresh();
                }
            }
        });
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
