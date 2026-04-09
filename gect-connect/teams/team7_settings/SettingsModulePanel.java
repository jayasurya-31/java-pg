package teams.team7_settings;

import shared.UIUtils;
import shared.SessionManager;
import shared.EventBus;
import shared.DebugLogger;
import core.User;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main panel for Settings & Feed module.
 * Manages Settings and Feed panels.
 */
public class SettingsModulePanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final FeedService feedService;
    private final SettingsService settingsService;
    private JPanel feedList;
    private JComboBox<String> themeBox;
    private JComboBox<String> privacyBox;
    private JCheckBox notificationsToggle;

    public SettingsModulePanel() {
        DebugLogger.info("SETTINGS ? Initializing module - constructor() - SettingsModulePanel.java");
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        this.feedService = new FeedService();
        this.settingsService = new SettingsService();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        UIUtils.stylePanel(container);

        container.add(createSettingsPanel(), "SETTINGS");
        container.add(createFeedPanel(), "FEED");

        add(container, BorderLayout.CENTER);

        // Module navigation
        JPanel moduleNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        moduleNav.setBackground(Color.WHITE);
        
        JButton feedBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        feedBtn.setLayout(new BorderLayout(10, 0));
        feedBtn.setPreferredSize(new Dimension(200, UIUtils.BUTTON_HEIGHT));
        feedBtn.add(new JLabel(shared.Icons.get("feed")), BorderLayout.WEST);
        JLabel feedText = new JLabel("Campus Feed");
        feedText.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        feedText.setForeground(Color.WHITE);
        feedBtn.add(feedText, BorderLayout.CENTER);
        feedBtn.addActionListener(e -> {
            DebugLogger.info("SETTINGS ? NAV_CAMPUS_FEED on feedBtn - actionPerformed() - SettingsModulePanel.java");
            cardLayout.show(container, "FEED");
        });
        moduleNav.add(feedBtn);

        JButton settingsBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        settingsBtn.setLayout(new BorderLayout(10, 0));
        settingsBtn.setPreferredSize(new Dimension(180, UIUtils.BUTTON_HEIGHT));
        settingsBtn.add(new JLabel(shared.Icons.get("settings")), BorderLayout.WEST);
        JLabel settingsText = new JLabel("Settings");
        settingsText.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        settingsText.setForeground(Color.WHITE);
        settingsBtn.add(settingsText, BorderLayout.CENTER);
        settingsBtn.addActionListener(e -> {
            DebugLogger.info("SETTINGS ? NAV_SETTINGS on settingsBtn - actionPerformed() - SettingsModulePanel.java");
            cardLayout.show(container, "SETTINGS");
        });
        moduleNav.add(settingsBtn);

        add(moduleNav, BorderLayout.NORTH);

        // Subscribe to events for UI updates
        DebugLogger.info("SETTINGS ? Subscribing to FEED_UPDATED - constructor() - SettingsModulePanel.java");
        EventBus.getInstance().subscribe("FEED_UPDATED", data -> refreshFeed());

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing SettingsModulePanel after login for user: " + user.getId());
                loadUserSettings();
                refreshFeed();
                cardLayout.show(container, "FEED");
            }
        });
        
        // Load initial settings
        loadUserSettings();
    }

    private void loadUserSettings() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        new Thread(() -> {
            core.UserSettings settings = settingsService.getSettings();
            SwingUtilities.invokeLater(() -> {
                themeBox.setSelectedItem(settings.getTheme());
                privacyBox.setSelectedItem(settings.getPrivacy());
                notificationsToggle.setSelected(settings.isNotificationsEnabled());
            });
        }).start();
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UIUtils.stylePanel(panel);
        
        JPanel neumorphicCard = UIUtils.createNeumorphicPanel(20);
        neumorphicCard.setLayout(new GridBagLayout());
        neumorphicCard.setPreferredSize(new Dimension(500, 500));
        neumorphicCard.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        UIUtils.applyHoverLift(neumorphicCard);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Application Settings", JLabel.CENTER);
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        title.setForeground(UIUtils.SECONDARY_COLOR);
        neumorphicCard.add(title, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 5, 10);
        themeBox = new JComboBox<>(new String[]{"LIGHT", "DARK"});
        themeBox.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        themeBox.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        themeBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR), "App Theme"));
        themeBox.setBackground(Color.WHITE);
        themeBox.addActionListener(e -> saveSettings());
        neumorphicCard.add(themeBox, gbc);

        gbc.gridy++;
        privacyBox = new JComboBox<>(new String[]{"PUBLIC", "PRIVATE", "CONTACTS"});
        privacyBox.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        privacyBox.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        privacyBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR), "Privacy Level"));
        privacyBox.setBackground(Color.WHITE);
        privacyBox.addActionListener(e -> saveSettings());
        neumorphicCard.add(privacyBox, gbc);
        
        gbc.gridy++;
        notificationsToggle = new JCheckBox("Enable Notifications");
        notificationsToggle.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        notificationsToggle.setOpaque(false);
        notificationsToggle.addActionListener(e -> saveSettings());
        neumorphicCard.add(notificationsToggle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 10, 10);
        JButton logoutBtn = UIUtils.createRoundedButton("Logout", Color.RED, Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        logoutBtn.addActionListener(e -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                new teams.team1_auth.AuthService().logout(currentUser);
            }
            SessionManager.getInstance().logout();
            JOptionPane.showMessageDialog(this, "Logged out successfully!");
            // Integration: Trigger global logout to MainFrame
            EventBus.getInstance().publish("GLOBAL_LOGOUT", null);
        });
        neumorphicCard.add(logoutBtn, gbc);

        panel.add(neumorphicCard);
        return panel;
    }

    private void saveSettings() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        String theme = (String) themeBox.getSelectedItem();
        String privacy = (String) privacyBox.getSelectedItem();
        boolean notifications = notificationsToggle.isSelected();

        new Thread(() -> {
            core.UserSettings settings = new core.UserSettings(0, theme, privacy, notifications);
            if (settingsService.updateSettings(settings)) {
                SwingUtilities.invokeLater(() -> {
                    UIUtils.toggleDarkMode("DARK".equals(theme));
                    UIUtils.showToast("Settings saved!", this);
                });
            }
        }).start();
    }

    private JPanel createFeedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        UIUtils.stylePanel(panel);
        
        JPanel postPanel = new JPanel(new BorderLayout(15, 0));
        postPanel.setBackground(Color.WHITE);
        postPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JTextField feedInput = UIUtils.createModernTextField("What's happening in GECT?");
        feedInput.setPreferredSize(new Dimension(0, UIUtils.INPUT_HEIGHT));
        
        JButton postBtn = UIUtils.createRoundedButton("Post", UIUtils.PRIMARY_COLOR, Color.WHITE);
        postBtn.setPreferredSize(new Dimension(120, UIUtils.BUTTON_HEIGHT));
        postBtn.addActionListener(e -> {
            String content = feedInput.getText();
            if (!content.isEmpty()) {
                feedService.post(content);
                feedInput.setText("");
            }
        });
        
        postPanel.add(feedInput, BorderLayout.CENTER);
        postPanel.add(postBtn, BorderLayout.EAST);
        panel.add(postPanel, BorderLayout.NORTH);

        feedList = new JPanel();
        feedList.setLayout(new BoxLayout(feedList, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(feedList);
        
        panel.add(new JScrollPane(feedList), BorderLayout.CENTER);
        
        refreshFeed();
        
        return panel;
    }

    private void refreshFeed() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        new Thread(() -> {
            List<String> items = feedService.getFeed(self.getId());
            SwingUtilities.invokeLater(() -> {
                feedList.removeAll();
                if (items.isEmpty()) {
                    JLabel empty = new JLabel("No posts yet. Be the first to post!", JLabel.CENTER);
                    empty.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
                    feedList.add(empty);
                } else {
                    for (String item : items) {
                        feedList.add(createFeedCard(item));
                    }
                }
                feedList.revalidate();
                feedList.repaint();
            });
        }).start();
    }

    private JPanel createFeedCard(String content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel contentLabel = new JLabel("<html>" + content + "</html>");
        contentLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        card.add(contentLabel, BorderLayout.CENTER);

        JLabel timeLabel = new JLabel("Just now");
        timeLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, UIUtils.CAPTION));
        timeLabel.setForeground(Color.GRAY);
        card.add(timeLabel, BorderLayout.SOUTH);

        return card;
    }
}
