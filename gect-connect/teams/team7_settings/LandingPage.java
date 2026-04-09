package teams.team7_settings;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import shared.*;
import teams.team4_chat.ChatService;
import teams.team5_group.GroupChatService;

/**
 * Modern futuristic green-themed LandingPage.
 */
public class LandingPage extends JPanel {

    private final JPanel sidebar;
    private final JPanel mainContent;
    private final CardLayout mainCardLayout;
    private final JPanel topNavbar;

    private final int sidebarWidthCollapsed = 70;
    private final int sidebarWidthExpanded = 250;
    private boolean isExpanded = false;

    private final List<SidebarButton> sidebarButtons = new ArrayList<>();
    private String currentTab = "WELCOME";
    private static boolean isFirstLogin = true;
    private teams.team2_profile.ProfileModulePanel profileModulePanel;

    public LandingPage() {
        DebugLogger.info("HOME → Initializing Modern LandingPage");

        setLayout(new BorderLayout());
        setBackground(UIUtils.BACKGROUND_COLOR);

        // Navbar
        topNavbar = createTopNavbar();
        add(topNavbar, BorderLayout.NORTH);

        // Sidebar
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main Content
        mainCardLayout = new CardLayout();
        mainContent = new WatermarkPanel(mainCardLayout);
        mainContent.setBackground(UIUtils.BACKGROUND_COLOR);
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainContent.add(new WelcomeTabPanel(), "WELCOME");
        mainContent.add(new teams.team4_chat.ChatModulePanel(), "CHATS");
        mainContent.add(new teams.team5_group.GroupChatModulePanel(), "GROUPS");
        mainContent.add(new teams.team6_notifications.NotificationModulePanel(), "NOTIFICATIONS");
        mainContent.add(new teams.team3_contacts.ContactsModulePanel(), "CONTACTS");
        
        profileModulePanel = new teams.team2_profile.ProfileModulePanel();
        mainContent.add(profileModulePanel, "PROFILE");
        
        mainContent.add(new teams.team7_settings.SettingsModulePanel(), "SETTINGS");

        add(mainContent, BorderLayout.CENTER);
        
        // Initial state
        mainCardLayout.show(mainContent, "WELCOME");
        updateActiveTab("WELCOME");

        if (isFirstLogin) {
            SwingUtilities.invokeLater(this::showInstructionsPopup);
            isFirstLogin = false;
        }

        setupEventListeners();
    }

    private void setupEventListeners() {
        EventBus.getInstance().subscribe("OPEN_CHAT_WINDOW", e -> switchTab("CHATS"));
        EventBus.getInstance().subscribe("OPEN_GROUP_CHAT", e -> switchTab("GROUPS"));
        EventBus.getInstance().subscribe("OPEN_PROFILE", e -> switchTab("PROFILE"));
    }

    // ========================= NAVBAR =========================

    private JPanel createTopNavbar() {
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Soft bottom shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        nav.setOpaque(false);
        nav.setPreferredSize(new Dimension(0, 65));
        nav.setBackground(UIUtils.NAVBAR_BG);

        // Left Section: Logo & Title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 16));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel(Icons.get("main_logo"));
        JLabel titleLabel = new JLabel("GECT Connect");
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        titleLabel.setForeground(Color.WHITE);

        leftPanel.add(logoLabel);
        leftPanel.add(titleLabel);
        nav.add(leftPanel, BorderLayout.WEST);

        // Right Section: Menu
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 16));
        rightPanel.setOpaque(false);

        JButton menuBtn = new JButton(Icons.get("more"));
        menuBtn.setPreferredSize(new Dimension(40, 40));
        menuBtn.setBorderPainted(false);
        menuBtn.setContentAreaFilled(false);
        menuBtn.setFocusPainted(false);
        menuBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu popup = new JPopupMenu();
        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> performLogout());
        popup.add(logout);

        menuBtn.addActionListener(e -> popup.show(menuBtn, 0, menuBtn.getHeight()));

        rightPanel.add(menuBtn);
        nav.add(rightPanel, BorderLayout.EAST);

        return nav;
    }

    // ========================= SIDEBAR =========================

    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UIUtils.SIDEBAR_BG);
        side.setPreferredSize(new Dimension(sidebarWidthCollapsed, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIUtils.BORDER_COLOR));

        // Toggle Button
        JButton toggleBtn = new JButton(Icons.get("menu"));
        toggleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        toggleBtn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setHorizontalAlignment(SwingConstants.CENTER);
        
        toggleBtn.addActionListener(e -> toggleSidebar());
        side.add(toggleBtn);
        side.add(Box.createVerticalStrut(10));

        // Navigation Tabs
        addSidebarTab(side, "home", "Home", "WELCOME");
        addSidebarTab(side, "chats", "Chats", "CHATS");
        addSidebarTab(side, "groups", "Groups", "GROUPS");
        addSidebarTab(side, "notifications", "Notifications", "NOTIFICATIONS");
        addSidebarTab(side, "contacts", "Contacts", "CONTACTS");

        side.add(Box.createVerticalGlue());

        // Bottom Section
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIUtils.BORDER_COLOR);
        side.add(sep);
        side.add(Box.createVerticalStrut(10));

        addSidebarTab(side, "profile", "Profile", "PROFILE");
        addSidebarTab(side, "settings", "Settings", "SETTINGS");
        side.add(Box.createVerticalStrut(10));

        return side;
    }

    private void addSidebarTab(JPanel side, String key, String title, String target) {
        SidebarButton btn = new SidebarButton(key, title, target);
        sidebarButtons.add(btn);
        side.add(btn);
        side.add(Box.createVerticalStrut(5));
    }

    private void toggleSidebar() {
        isExpanded = !isExpanded;
        int targetWidth = isExpanded ? sidebarWidthExpanded : sidebarWidthCollapsed;

        Timer timer = new Timer(5, null);
        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentWidth = sidebar.getWidth();
                int step = 20;

                if (isExpanded && currentWidth < targetWidth) {
                    int nextWidth = Math.min(currentWidth + step, targetWidth);
                    sidebar.setPreferredSize(new Dimension(nextWidth, 0));
                } else if (!isExpanded && currentWidth > targetWidth) {
                    int nextWidth = Math.max(currentWidth - step, targetWidth);
                    sidebar.setPreferredSize(new Dimension(nextWidth, 0));
                } else {
                    sidebar.setPreferredSize(new Dimension(targetWidth, 0));
                    for (SidebarButton btn : sidebarButtons) {
                        btn.setExpanded(isExpanded);
                    }
                    ((Timer) e.getSource()).stop();
                }
                sidebar.revalidate();
                sidebar.repaint();
            }
        });
        timer.start();
    }

    private void switchTab(String target) {
        if (currentTab.equals(target)) return;
        currentTab = target;
        mainCardLayout.show(mainContent, target);
        
        // Requirement: Reset Profile module to View Profile every time it's accessed
        if ("PROFILE".equals(target) && profileModulePanel != null) {
            profileModulePanel.showScreen("VIEW_PROFILE");
        }
        
        updateActiveTab(target);
    }

    private void updateActiveTab(String target) {
        for (SidebarButton btn : sidebarButtons) {
            btn.setActive(btn.target.equals(target));
        }
    }

    private void performLogout() {
        SessionManager.getInstance().logoutCurrentUser();
        EventBus.getInstance().publish("GLOBAL_LOGOUT", null);
    }

    private void showInstructionsPopup() {
        JOptionPane.showMessageDialog(this,
                "Step 1: Explore your profile\nStep 2: Check chats\nStep 3: View notifications",
                "Getting Started",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Custom Sidebar Button with active state and indicator.
     */
    private class SidebarButton extends JButton {
        private final String key;
        private final String title;
        private final String target;
        private boolean isActive = false;
        private float hoverProgress = 0.0f;
        private Timer hoverTimer;
        private float scale = 1.0f;

        public SidebarButton(String key, String title, String target) {
            this.key = key;
            this.title = title;
            this.target = target;

            setIcon(Icons.get(key));
            setToolTipText(title);
            setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 15));
            
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            setHorizontalAlignment(SwingConstants.LEFT);
            setMargin(new Insets(0, 0, 0, 0));
            setIconTextGap(20);

            addActionListener(e -> {
                // Click effect: brief scale down
                scale = 0.95f;
                repaint();
                Timer t = new Timer(120, event -> {
                    scale = 1.0f;
                    repaint();
                });
                t.setRepeats(false);
                t.start();
                switchTab(target);
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    startHoverAnimation(true);
                    scale = 1.1f; // Icon scale up
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    startHoverAnimation(false);
                    scale = 1.0f;
                    repaint();
                }
            });
        }

        private void startHoverAnimation(boolean in) {
            if (hoverTimer != null) hoverTimer.stop();
            hoverTimer = new Timer(10, e -> {
                if (in) {
                    hoverProgress += 0.1f;
                    if (hoverProgress >= 1.0f) {
                        hoverProgress = 1.0f;
                        ((Timer)e.getSource()).stop();
                    }
                } else {
                    hoverProgress -= 0.1f;
                    if (hoverProgress <= 0.0f) {
                        hoverProgress = 0.0f;
                        ((Timer)e.getSource()).stop();
                    }
                }
                repaint();
            });
            hoverTimer.start();
        }

        public void setExpanded(boolean expanded) {
            if (expanded) {
                setText(title);
                setHorizontalAlignment(SwingConstants.LEFT);
                setMargin(new Insets(0, 23, 0, 0));
            } else {
                setText("");
                setHorizontalAlignment(SwingConstants.CENTER);
                setMargin(new Insets(0, 0, 0, 0));
            }
            revalidate();
            repaint();
        }

        public void setActive(boolean active) {
            this.isActive = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Hover/Active background
            if (isActive) {
                g2.setColor(UIUtils.ACTIVE_COLOR);
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
                setForeground(UIUtils.ACTIVE_TEXT);
            } else if (hoverProgress > 0) {
                g2.setColor(new Color(
                    UIUtils.HOVER_COLOR.getRed(),
                    UIUtils.HOVER_COLOR.getGreen(),
                    UIUtils.HOVER_COLOR.getBlue(),
                    (int)(hoverProgress * 255)
                ));
                g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
                setForeground(UIUtils.TEXT_PRIMARY);
            } else {
                setForeground(UIUtils.TEXT_PRIMARY);
            }

            // Scaling effect
            if (scale != 1.0f) {
                g2.scale(scale, scale);
                // Adjust position to scale from center
                float moveX = (getWidth() * (1 - scale)) / 2;
                float moveY = (getHeight() * (1 - scale)) / 2;
                g2.translate(moveX / scale, moveY / scale);
            }

            super.paintComponent(g2);

            // Indicator
            if (isActive) {
                g2.setColor(UIUtils.DARK_PRIMARY);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            }
            g2.dispose();
        }
    }
}
