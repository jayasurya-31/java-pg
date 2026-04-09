package teams.team6_notifications;

import core.User;
import core.Notification;
import shared.UIUtils;
import shared.SessionManager;
import shared.EventBus;
import shared.DebugLogger;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Main panel for Notifications & Media module.
 * Manages Notifications and Media Preview panels.
 */
public class NotificationModulePanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final NotificationService notificationService;
    private JPanel notificationList;
    private JLabel imagePreviewLabel;

    public NotificationModulePanel() {
        DebugLogger.info("NOTIFICATIONS ? Initializing module - constructor() - NotificationModulePanel.java");
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        this.notificationService = new NotificationService();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        UIUtils.stylePanel(container);

        container.add(createNotificationPanel(), "NOTIFICATIONS");
        container.add(createMediaPanel(), "MEDIA");

        add(container, BorderLayout.CENTER);

        // Sidebar inside module for navigation
        JPanel moduleNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        moduleNav.setBackground(Color.WHITE);
        
        JButton notifBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        notifBtn.setLayout(new BorderLayout(10, 0));
        notifBtn.setPreferredSize(new Dimension(200, UIUtils.BUTTON_HEIGHT));
        notifBtn.add(new JLabel(shared.Icons.get("notifications")), BorderLayout.WEST);
        JLabel notifText = new JLabel("Notifications");
        notifText.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        notifText.setForeground(Color.WHITE);
        notifBtn.add(notifText, BorderLayout.CENTER);
        notifBtn.addActionListener(e -> {
            DebugLogger.info("NOTIFICATIONS ? NAV_NOTIFICATIONS on notifBtn - actionPerformed() - NotificationModulePanel.java");
            cardLayout.show(container, "NOTIFICATIONS");
        });
        moduleNav.add(notifBtn);

        JButton mediaBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        mediaBtn.setLayout(new BorderLayout(10, 0));
        mediaBtn.setPreferredSize(new Dimension(220, UIUtils.BUTTON_HEIGHT));
        mediaBtn.add(new JLabel(shared.Icons.get("media")), BorderLayout.WEST);
        JLabel mediaText = new JLabel("Media Preview");
        mediaText.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        mediaText.setForeground(Color.WHITE);
        mediaBtn.add(mediaText, BorderLayout.CENTER);
        mediaBtn.addActionListener(e -> {
            DebugLogger.info("NOTIFICATIONS ? NAV_MEDIA_PREVIEW on mediaBtn - actionPerformed() - NotificationModulePanel.java");
            cardLayout.show(container, "MEDIA");
        });
        moduleNav.add(mediaBtn);

        add(moduleNav, BorderLayout.NORTH);

        // Subscribe to events for UI updates
        DebugLogger.info("NOTIFICATIONS ? Subscribing to NOTIFICATION_RECEIVED - constructor() - NotificationModulePanel.java");
        EventBus.getInstance().subscribe("NOTIFICATION_RECEIVED", data -> refreshNotifications());

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing NotificationModulePanel after login for user: " + user.getId());
                refreshNotifications();
                cardLayout.show(container, "NOTIFICATIONS");
            }
        });
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        UIUtils.stylePanel(panel);
        
        JLabel title = new JLabel("  Your Notifications");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        panel.add(title, BorderLayout.NORTH);
        
        notificationList = new JPanel();
        notificationList.setLayout(new BoxLayout(notificationList, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(notificationList);
        
        panel.add(new JScrollPane(notificationList), BorderLayout.CENTER);
        
        refreshNotifications();
        
        // Mark all as read when opening this panel
        User self = SessionManager.getInstance().getCurrentUser();
        if (self != null) {
            new Thread(() -> {
                notificationService.markAllAsRead(self.getId());
                EventBus.getInstance().publish("NOTIFICATIONS_READ", null);
            }).start();
        }
        
        return panel;
    }

    private JPanel createMediaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UIUtils.stylePanel(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;
        
        JLabel title = new JLabel("Media Preview & Upload");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        panel.add(title, gbc);

        gbc.gridy++;
        imagePreviewLabel = new JLabel("No Preview Available");
        imagePreviewLabel.setPreferredSize(new Dimension(300, 300));
        imagePreviewLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imagePreviewLabel, gbc);
        
        gbc.gridy++;
        JButton uploadBtn = UIUtils.createRoundedButton("Select & Upload Media", UIUtils.PRIMARY_COLOR, Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            // Allowed types: jpg, png, pdf, docx
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Allowed Files (jpg, png, pdf, docx)", "jpg", "png", "pdf", "docx"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File source = fileChooser.getSelectedFile();
                
                // Max size check: 10MB
                long fileSizeInMB = source.length() / (1024 * 1024);
                if (fileSizeInMB > 10) {
                    UIUtils.showToast("File too large (max 10MB)", this);
                    return;
                }

                // Extension check
                String name = source.getName().toLowerCase();
                if (!(name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pdf") || name.endsWith(".docx"))) {
                    UIUtils.showToast("Invalid file type", this);
                    return;
                }

                // Live preview before upload (or after)
                if (name.endsWith(".jpg") || name.endsWith(".png")) {
                    try {
                        ImageIcon icon = new ImageIcon(source.getAbsolutePath());
                        Image img = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                        imagePreviewLabel.setIcon(new ImageIcon(img));
                        imagePreviewLabel.setText("");
                    } catch (Exception ex) {
                        imagePreviewLabel.setText("Preview Error");
                    }
                } else {
                    imagePreviewLabel.setIcon(null);
                    imagePreviewLabel.setText("File: " + source.getName());
                }

                File destDir = new File("uploads");
                if (!destDir.exists()) destDir.mkdirs();
                
                File destFile = new File(destDir, source.getName());
                try {
                    Files.copy(source.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    UIUtils.showToast("Media uploaded: " + source.getName(), this);
                    
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        notificationService.notify(currentUser.getId(), "Media Uploaded", source.getName()); // Title and content
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    UIUtils.showToast("Upload failed!", this);
                }
            }
        });
        panel.add(uploadBtn, gbc);
        
        return panel;
    }

    private void refreshNotifications() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        new Thread(() -> {
            List<Notification> items = notificationService.getNotifications();
            SwingUtilities.invokeLater(() -> {
                notificationList.removeAll();
                if (items.isEmpty()) {
                    JLabel empty = new JLabel("No new notifications", JLabel.CENTER);
                    empty.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
                    notificationList.add(empty);
                } else {
                    for (Notification item : items) {
                        notificationList.add(createNotificationCard(item));
                    }
                }
                notificationList.revalidate();
                notificationList.repaint();
            });
        }).start();
    }

    private JPanel createNotificationCard(Notification notification) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBackground(notification.isRead() ? Color.WHITE : Color.decode("#f0f7ff"));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel msgLabel = new JLabel("[" + notification.getType() + "] " + notification.getMessage());
        msgLabel.setFont(new Font(UIUtils.FONT_FAMILY, notification.isRead() ? Font.PLAIN : Font.BOLD, UIUtils.BODY_SMALL));
        card.add(msgLabel, BorderLayout.CENTER);

        if (!notification.isRead()) {
            JButton readBtn = UIUtils.createRoundedButton("Mark as Read", UIUtils.PRIMARY_COLOR, Color.WHITE);
            readBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
            readBtn.setPreferredSize(new Dimension(140, 40));
            readBtn.addActionListener(e -> {
                new Thread(() -> {
                    if (notificationService.markAsRead(notification.getId())) {
                        SwingUtilities.invokeLater(() -> {
                            refreshNotifications();
                            EventBus.getInstance().publish("NOTIFICATIONS_READ", null);
                        });
                    }
                }).start();
            });
            card.add(readBtn, BorderLayout.EAST);
        }

        return card;
    }
}
