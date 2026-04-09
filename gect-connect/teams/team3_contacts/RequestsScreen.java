package teams.team3_contacts;
import teams.team3_contacts.ContactCardFactory;
import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RequestsScreen extends JPanel {
    private final ContactsService contactsService;
    private final JPanel requestsReceivedPanel;
    private final JPanel requestsSentPanel;

    public RequestsScreen(ContactsService contactsService) {
        this.contactsService = contactsService;
        setLayout(new GridLayout(2, 1));
        UIUtils.stylePanel(this);

        JPanel received = new JPanel(new BorderLayout());
        UIUtils.stylePanel(received);
        received.add(createHeaderLabel("Received Requests"), BorderLayout.NORTH);
        requestsReceivedPanel = new JPanel();
        requestsReceivedPanel.setLayout(new BoxLayout(requestsReceivedPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(requestsReceivedPanel);
        received.add(new JScrollPane(requestsReceivedPanel), BorderLayout.CENTER);

        JPanel sent = new JPanel(new BorderLayout());
        UIUtils.stylePanel(sent);
        sent.add(createHeaderLabel("Sent Requests"), BorderLayout.NORTH);
        requestsSentPanel = new JPanel();
        requestsSentPanel.setLayout(new BoxLayout(requestsSentPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(requestsSentPanel);
        sent.add(new JScrollPane(requestsSentPanel), BorderLayout.CENTER);

        add(received);
        add(sent);

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing RequestsScreen after login for user: " + user.getId());
                refreshRequests();
            }
        });
    }

    public void refreshRequests() {
        System.out.println("[DEBUG] refreshRequests() called. Current user: " + SessionManager.getInstance().getCurrentUser());
        DebugLogger.info("REQUESTS → refreshRequests() called.");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<User> received, sent;
            @Override
            protected Void doInBackground() {
                DebugLogger.info("REQUESTS → Fetching received and sent requests from service...");
                received = contactsService.getReceivedRequests();
                sent = contactsService.getSentRequests();
                System.out.println("[DEBUG] Requests fetched: Received=" + (received != null ? received.size() : 0) + ", Sent=" + (sent != null ? sent.size() : 0) + ", Data Received=" + received + ", Data Sent=" + sent);
                DebugLogger.info("REQUESTS → Received: " + (received != null ? received.size() : 0) + " | Sent: " + (sent != null ? sent.size() : 0));
                return null;
            }
            @Override
            protected void done() {
                DebugLogger.info("REQUESTS → Updating UI panels.");
                
                requestsReceivedPanel.removeAll();
                if (received != null && !received.isEmpty()) {
                    DebugLogger.info("REQUESTS → Creating cards for " + received.size() + " received requests.");
                    for (User u : received) requestsReceivedPanel.add(createRequestCard(u, true));
                } else {
                    DebugLogger.info("REQUESTS → No received requests found.");
                    requestsReceivedPanel.add(createEmptyLabel("No pending received requests"));
                }

                requestsSentPanel.removeAll();
                if (sent != null && !sent.isEmpty()) {
                    DebugLogger.info("REQUESTS → Creating cards for " + sent.size() + " sent requests.");
                    for (User u : sent) requestsSentPanel.add(createRequestCard(u, false));
                } else {
                    DebugLogger.info("REQUESTS → No sent requests found.");
                    requestsSentPanel.add(createEmptyLabel("No pending sent requests"));
                }

                requestsReceivedPanel.revalidate(); requestsReceivedPanel.repaint();
                requestsSentPanel.revalidate(); requestsSentPanel.repaint();
                DebugLogger.info("REQUESTS → UI update complete.");
            }
        };
        worker.execute();
    }

    private JPanel createRequestCard(User u, boolean isReceived) {
        String type = isReceived ? "RECEIVED" : "SENT";
        DebugLogger.info("REQUESTS → [" + type + "] Creating request card for User ID: " + u.getId() + " | Name: " + u.getFullName());
        
        JPanel card = ContactCardFactory.createBaseCard(u);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        if (isReceived) {
            JButton accept = UIUtils.createRoundedButton("Accept", UIUtils.PRIMARY_COLOR, Color.WHITE);
            accept.setPreferredSize(new Dimension(100, 40));
            accept.addActionListener(e -> {
                DebugLogger.info("REQUESTS → [RECEIVED] Accept clicked for User ID: " + u.getId());
                boolean success = contactsService.acceptRequest(u.getId());
                DebugLogger.info("REQUESTS → [RECEIVED] Result of acceptRequest(" + u.getId() + "): " + success);
                if (success) {
                    UIUtils.showToast("Request accepted!", this);
                    refreshRequests();
                }
            });

            JButton reject = UIUtils.createRoundedButton("Reject", Color.WHITE, Color.DARK_GRAY);
            reject.setPreferredSize(new Dimension(100, 40));
            reject.addActionListener(e -> {
                DebugLogger.info("REQUESTS → [RECEIVED] Reject clicked for User ID: " + u.getId());
                boolean success = contactsService.rejectRequest(u.getId());
                DebugLogger.info("REQUESTS → [RECEIVED] Result of rejectRequest(" + u.getId() + "): " + success);
                if (success) {
                    UIUtils.showToast("Request rejected", this);
                    refreshRequests();
                }
            });

            JButton block = UIUtils.createRoundedButton("Block", Color.WHITE, Color.RED);
            block.setPreferredSize(new Dimension(100, 40));
            block.addActionListener(e -> {
                DebugLogger.info("REQUESTS → [RECEIVED] Block clicked for User ID: " + u.getId());
                if (JOptionPane.showConfirmDialog(this, "Block " + u.getFullName() + "?", "Confirm", 0) == 0) {
                    DebugLogger.info("REQUESTS → [RECEIVED] User confirmed block of User ID: " + u.getId());
                    boolean success = contactsService.blockUser(u.getId());
                    DebugLogger.info("REQUESTS → [RECEIVED] Result of blockUser(" + u.getId() + "): " + success);
                    if (success) {
                        UIUtils.showToast("User blocked", this);
                        refreshRequests();
                    }
                } else {
                    DebugLogger.info("REQUESTS → [RECEIVED] User cancelled block of User ID: " + u.getId());
                }
            });

            actions.add(accept); actions.add(reject); actions.add(block);
        } else {
            JButton cancel = UIUtils.createRoundedButton("Cancel", Color.WHITE, Color.DARK_GRAY);
            cancel.setPreferredSize(new Dimension(100, 40));
            cancel.addActionListener(e -> {
                DebugLogger.info("REQUESTS → [SENT] Cancel clicked for User ID: " + u.getId());
                boolean success = contactsService.cancelRequest(u.getId());
                DebugLogger.info("REQUESTS → [SENT] Result of cancelRequest(" + u.getId() + "): " + success);
                if (success) {
                    UIUtils.showToast("Request cancelled", this);
                    refreshRequests();
                }
            });
            actions.add(cancel);
        }

        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        label.setPreferredSize(new Dimension(0, 50));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }

    private JLabel createEmptyLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, 16));
        label.setForeground(Color.GRAY);
        label.setPreferredSize(new Dimension(0, 100));
        return label;
    }
}
