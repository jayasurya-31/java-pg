package teams.team3_contacts;

import shared.*;
import javax.swing.*;
import java.awt.*;

/**
 * Main panel for Team 3: Contact Management Module.
 * Manages navigation between MyContactsScreen, SearchScreen, RequestsScreen, and BlockedScreen.
 */
public class ContactsModulePanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final ContactsService contactsService;
    
    private final MyContactsScreen contactsScreen;
    private final SearchScreen searchScreen;
    private final RequestsScreen requestsScreen;
    private final BlockedScreen blockedScreen;

    public ContactsModulePanel() {
        this.contactsService = new ContactsService();
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        UIUtils.stylePanel(container);

        // Initialize screens
        contactsScreen = new MyContactsScreen(contactsService);
        searchScreen = new SearchScreen(contactsService);
        requestsScreen = new RequestsScreen(contactsService);
        blockedScreen = new BlockedScreen(contactsService);

        // Add screens to container
        container.add(contactsScreen, "CONTACTS");
        container.add(searchScreen, "SEARCH");
        container.add(requestsScreen, "REQUESTS");
        container.add(blockedScreen, "BLOCKED");

        // UI Setup
        setupTopNavigation();
        add(container, BorderLayout.CENTER);

        // Event handling
        setupEventSubscriptions();

        // Initial data load
        refreshAllData();
    }

    private void setupTopNavigation() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        nav.setBackground(Color.WHITE);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR));

        String[] labels = {"My Contacts", "Search", "Requests", "Blocked"};
        String[] cards = {"CONTACTS", "SEARCH", "REQUESTS", "BLOCKED"};

        for (int i = 0; i < labels.length; i++) {
            final String card = cards[i];
            JButton btn = UIUtils.createRoundedButton(labels[i], UIUtils.PRIMARY_COLOR, Color.WHITE);
            btn.setPreferredSize(new Dimension(140, 40));
            btn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 14));
            btn.addActionListener(e -> cardLayout.show(container, card));
            nav.add(btn);
        }
        add(nav, BorderLayout.NORTH);
    }

    private void setupEventSubscriptions() {
        EventBus.getInstance().subscribe("CONTACT_LIST_UPDATED", data -> {
            contactsScreen.refreshContacts();
            searchScreen.refresh();
        });
        EventBus.getInstance().subscribe("CONTACT_REQUEST_UPDATED", data -> {
            requestsScreen.refreshRequests();
            searchScreen.refresh();
        });
        EventBus.getInstance().subscribe("BLOCKED_LIST_UPDATED", data -> {
            blockedScreen.refreshBlocked();
            searchScreen.refresh();
        });
    }

    private void refreshAllData() {
        contactsScreen.refreshContacts();
        requestsScreen.refreshRequests();
        blockedScreen.refreshBlocked();
        searchScreen.refresh();
    }
}
