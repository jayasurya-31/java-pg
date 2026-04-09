package integration;

import core.User;
import java.awt.*;
import javax.swing.*;
import shared.DebugLogger;
import shared.EventBus;
import shared.SessionManager;
import shared.UIUtils;
import teams.team1_auth.AuthModulePanel;
import teams.team2_profile.ProfileModulePanel;
import teams.team3_contacts.ContactsModulePanel;
import teams.team4_chat.ChatModulePanel;
import teams.team5_group.GroupChatModulePanel;
import teams.team6_notifications.NotificationModulePanel;
import teams.team7_settings.LandingPage;
import teams.team7_settings.SettingsModulePanel;

/**
 * Main application frame that integrates all modules.
 * Uses CardLayout to switch between different module screens.
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainContentPanel;
    private RouterController router;
    private ProfileModulePanel profileModulePanel;
    private JPanel sidebarPanel;
    private JPanel topBar;
    private JLabel userInfoLabel;
    private JButton activeNavButton;
    private boolean isSidebarCollapsed = false;

    public MainFrame() {
        setTitle("GECT Connect (Swing Edition)");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UIUtils.setModernLookAndFeel();

        // Main layout
        setLayout(new BorderLayout());

        // 3. Main Content Panel (Center) - Initialize first for Router
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        
        // Initialize RouterController
        router = new RouterController(mainContentPanel, cardLayout);

        // Initialize screens (need profileModulePanel for sidebar listener)
        initializeScreens();

        // 1. Sidebar Panel (Left)
        sidebarPanel = createSidebar();
        add(sidebarPanel, BorderLayout.WEST);

        // 2. Top Bar (Top)
        topBar = createTopBar();
        add(topBar, BorderLayout.NORTH);

        add(mainContentPanel, BorderLayout.CENTER);

        // Default state: Hide sidebar and topbar until login
        sidebarPanel.setVisible(false);
        topBar.setVisible(false);

        // Default screen
        router.showImmediately("LOGIN");

        // Listen for global login success
        EventBus.getInstance().subscribe("AUTH_LOGIN_SUCCESS", data -> {
            SwingUtilities.invokeLater(() -> {
                // Show LandingPage (HOME) which handles its own layout
                sidebarPanel.setVisible(false); // Hide global sidebar for LandingPage
                topBar.setVisible(false);      // Hide global topbar for LandingPage
                if (data instanceof User) {
                    User user = (User) data;
                    userInfoLabel.setText("Logged in as: " + user.getFullName() + "  ");
                }
                router.navigateTo("HOME");
            });
        });

        // Listen for route changes to toggle global navigation visibility
        EventBus.getInstance().subscribe("ROUTE_CHANGED", data -> {
            if (data instanceof String) {
                String route = (String) data;
                boolean isHome = "HOME".equals(route);
                boolean isLogin = "LOGIN".equals(route);
                
                SwingUtilities.invokeLater(() -> {
                    if (isLogin) {
                        sidebarPanel.setVisible(false);
                        topBar.setVisible(false);
                    } else if (isHome) {
                        sidebarPanel.setVisible(false); // HOME uses LandingPage layout
                        topBar.setVisible(false);
                    } else {
                        sidebarPanel.setVisible(true);
                        topBar.setVisible(true);
                    }
                    revalidate();
                    repaint();
                });
            }
        });

        // Listen for global logout
        EventBus.getInstance().subscribe("GLOBAL_LOGOUT", data -> {
            SwingUtilities.invokeLater(() -> {
                sidebarPanel.setVisible(false);
                topBar.setVisible(false);
                router.showImmediately("LOGIN");
            });
        });

        // Listen for internal navigation from modules
        EventBus.getInstance().subscribe("OPEN_PROFILE_SETTINGS", data -> {
            SwingUtilities.invokeLater(() -> {
                router.navigateTo("PROFILE");
            });
        });

        // Listen for theme changes
        EventBus.getInstance().subscribe("THEME_CHANGED", data -> {
            boolean isDark = (boolean) data;
            SwingUtilities.invokeLater(() -> {
                updateTheme(isDark);
            });
        });

        setVisible(true);
    }

    private void updateTheme(boolean isDark) {
        Color bg = UIUtils.getBackgroundColor();
        Color fg = UIUtils.getTextColor();
        
        mainContentPanel.setBackground(bg);
        topBar.setBackground(isDark ? bg.darker() : Color.WHITE);
        
        // Recurse through components to update colors
        updateComponentColors(this, bg, fg);
        
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void updateComponentColors(Container container, Color bg, Color fg) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                if (!(c instanceof UIUtils.FadePanel)) { // Don't force bg on animation panels
                    c.setBackground(bg);
                }
                updateComponentColors((Container) c, bg, fg);
            } else if (c instanceof JLabel) {
                c.setForeground(fg);
            } else if (c instanceof JButton) {
                // Keep primary buttons consistent but update text if needed
                if (((JButton)c).getForeground() != Color.WHITE) {
                    c.setForeground(fg);
                }
            } else if (c instanceof JTextField) {
                c.setForeground(fg);
                // Background for text fields in dark mode
                ((JTextField)c).setCaretColor(fg);
            } else if (c instanceof JScrollPane) {
                c.setBackground(bg);
                ((JScrollPane)c).getViewport().setBackground(bg);
                updateComponentColors((Container) c, bg, fg);
            }
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(UIUtils.PRIMARY_COLOR);
        sidebar.setLayout(new BorderLayout());

        // Sidebar Header (Logo)
        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 100));
        JLabel logoLabel = new JLabel("GECT Connect");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        header.add(logoLabel);
        sidebar.add(header, BorderLayout.NORTH);

        // Navigation Panel
        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Navigation buttons with specified items and icon fallbacks
        navPanel.add(createNavButton("Home", "HOME", "home", ""));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Profile", "PROFILE_MODULE", "profile", ""));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Contacts", "CONTACTS", "contacts", ""));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Chats", "CHAT", "chats", ""));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Groups", "GROUP_CHAT", "groups", ""));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Notifications", "NOTIFICATIONS", "notifications", ""));
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(createNavButton("Settings", "SETTINGS", "settings", ""));

        sidebar.add(new JScrollPane(navPanel) {{
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(null);
        }}, BorderLayout.CENTER);

        return sidebar;
    }

    private JButton createNavButton(String text, String screenName, String iconName, String emojiFallback) {
        JButton btn = UIUtils.createRoundedButton(text, UIUtils.PRIMARY_COLOR, Color.WHITE);
        btn.setIcon(shared.Icons.get(iconName));
        btn.setToolTipText(text);
        btn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setMargin(new Insets(0, 15, 0, 0));
        btn.setMaximumSize(new Dimension(200, 55));
        btn.setPreferredSize(new Dimension(200, 55));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btn.addActionListener(e -> {
            if (activeNavButton != null) {
                activeNavButton.setBackground(UIUtils.PRIMARY_COLOR);
            }
            activeNavButton = btn;
            activeNavButton.setBackground(UIUtils.PRIMARY_COLOR.darker());
            
            router.navigateTo(screenName);
            
            // Requirement: Reset Profile module to View Profile every time it's accessed
            if ("PROFILE_MODULE".equals(screenName) && profileModulePanel != null) {
                profileModulePanel.showScreen("VIEW_PROFILE");
            }
        });
        
        if (screenName.equals("HOME")) {
            activeNavButton = btn;
            activeNavButton.setBackground(UIUtils.PRIMARY_COLOR.darker());
        }
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != activeNavButton) btn.setBackground(UIUtils.PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != activeNavButton) btn.setBackground(UIUtils.PRIMARY_COLOR);
            }
        });
        
        return btn;
    }

    private void toggleSidebar() {
        isSidebarCollapsed = !isSidebarCollapsed;
        int targetWidth = isSidebarCollapsed ? 60 : 220;
        
        sidebarPanel.setPreferredSize(new Dimension(targetWidth, 0));
        
        // Update header and buttons visibility based on collapse state
        for (Component c : sidebarPanel.getComponents()) {
            if (c instanceof JPanel) { // Header
                c.setVisible(!isSidebarCollapsed);
            } else if (c instanceof JScrollPane) {
                JViewport viewport = ((JScrollPane) c).getViewport();
                JPanel navPanel = (JPanel) viewport.getView();
                for (Component btnComp : navPanel.getComponents()) {
                    if (btnComp instanceof JButton) {
                        JButton btn = (JButton) btnComp;
                        if (isSidebarCollapsed) {
                            btn.setText("");
                            btn.setHorizontalAlignment(SwingConstants.CENTER);
                            btn.setMargin(new Insets(0, 0, 0, 0));
                        } else {
                            // Find title from somewhere or hardcode mapping?
                            // Better: btn.getToolTipText()? But we didn't set it.
                            // Let's set it in createNavButton.
                            btn.setText(btn.getToolTipText());
                            btn.setHorizontalAlignment(SwingConstants.LEFT);
                            btn.setMargin(new Insets(0, 15, 0, 0));
                        }
                    }
                }
            }
        }
        
        revalidate();
        repaint();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(0, 70));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        leftPanel.setOpaque(false);

        JButton menuBtn = UIUtils.createRoundedButton("", Color.WHITE, UIUtils.PRIMARY_COLOR);
        menuBtn.setIcon(shared.Icons.get("menu"));
        menuBtn.setPreferredSize(new Dimension(50, 45));
        menuBtn.addActionListener(e -> toggleSidebar());
        leftPanel.add(menuBtn);

        JLabel titleLabel = new JLabel("GECT Connect");
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_LARGE));
        leftPanel.add(titleLabel);
        bar.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        rightPanel.setOpaque(false);

        userInfoLabel = new JLabel("");
        userInfoLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        rightPanel.add(userInfoLabel);

        JButton logoutBtn = UIUtils.createRoundedButton("Logout", Color.RED, Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(120, 45));
        logoutBtn.addActionListener(e -> {
            DebugLogger.info("UI ? CLICK_LOGOUT on logoutBtn - actionPerformed() - MainFrame.java");
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                new teams.team1_auth.AuthService().logout(currentUser);
            }
            SessionManager.getInstance().logout();
            EventBus.getInstance().publish("GLOBAL_LOGOUT", null);
        });
        rightPanel.add(logoutBtn);

        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }

    private void initializeScreens() {
        mainContentPanel.add(new AuthModulePanel(), "LOGIN");
        mainContentPanel.add(new LandingPage(), "HOME"); // Home/Feed is now LandingPage
        profileModulePanel = new ProfileModulePanel();
        mainContentPanel.add(profileModulePanel, "PROFILE_MODULE");
        mainContentPanel.add(new ContactsModulePanel(), "CONTACTS");
        mainContentPanel.add(new ChatModulePanel(), "CHAT");
        mainContentPanel.add(new GroupChatModulePanel(), "GROUP_CHAT");
        mainContentPanel.add(new NotificationModulePanel(), "NOTIFICATIONS");
        mainContentPanel.add(new SettingsModulePanel(), "SETTINGS");
    }

    public static void main(String[] args) {
        DebugLogger.info("UI ? Application started successfully - main() - MainFrame.java");
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
