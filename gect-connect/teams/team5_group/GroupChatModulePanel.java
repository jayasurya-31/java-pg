package teams.team5_group;

import shared.AnimationUtils;
import shared.UIUtils;
import shared.SessionManager;
import shared.EventBus;
import shared.DebugLogger;
import core.Group;
import core.GroupMessage;
import core.User;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Main panel for Group Chat module.
 * Manages Group List, Group Chat, and Create Group panels.
 */
public class GroupChatModulePanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final GroupChatService groupChatService;
    private JPanel groupListPanel;
    private JPanel chatHistory;
    private JScrollPane scrollPane;
    private Group currentGroup;
    private Timer pollingTimer;
    private int lastLoadedMessageId = 0;

    public GroupChatModulePanel() {
        DebugLogger.info("GROUP ? Initializing module - constructor() - GroupChatModulePanel.java");
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        this.groupChatService = new GroupChatService();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        UIUtils.stylePanel(container);

        container.add(createGroupListPanel(), "LIST");
        container.add(createGroupChatPanel(), "CHAT");
        container.add(createCreateGroupPanel(), "CREATE");

        add(container, BorderLayout.CENTER);

        // Sidebar inside module for navigation
        JPanel moduleNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        moduleNav.setBackground(Color.WHITE);
        
        JButton listBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        listBtn.setLayout(new BorderLayout(10, 0));
        listBtn.setPreferredSize(new Dimension(180, UIUtils.BUTTON_HEIGHT));
        listBtn.add(new JLabel(shared.Icons.get("groups")), BorderLayout.WEST);
        JLabel listText = new JLabel("My Groups");
        listText.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        listText.setForeground(Color.WHITE);
        listBtn.add(listText, BorderLayout.CENTER);
        listBtn.addActionListener(e -> {
            DebugLogger.info("GROUP ? NAV_GROUP_LIST on listBtn - actionPerformed() - GroupChatModulePanel.java");
            stopPolling();
            cardLayout.show(container, "LIST");
        });
        moduleNav.add(listBtn);

        JButton createBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        createBtn.setLayout(new BorderLayout(10, 0));
        createBtn.setPreferredSize(new Dimension(200, UIUtils.BUTTON_HEIGHT));
        createBtn.add(new JLabel(shared.Icons.get("add")), BorderLayout.WEST);
        JLabel createText = new JLabel("Create Group");
        createText.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        createText.setForeground(Color.WHITE);
        createBtn.add(createText, BorderLayout.CENTER);
        createBtn.addActionListener(e -> {
            DebugLogger.info("GROUP ? NAV_CREATE_GROUP on createBtn - actionPerformed() - GroupChatModulePanel.java");
            stopPolling();
            cardLayout.show(container, "CREATE");
        });
        moduleNav.add(createBtn);

        add(moduleNav, BorderLayout.NORTH);

        // Subscribe to events for UI updates
        DebugLogger.info("GROUP ? Subscribing to GROUP_CREATED - constructor() - GroupChatModulePanel.java");
        EventBus.getInstance().subscribe("GROUP_CREATED", data -> refreshGroups());
        
        DebugLogger.info("GROUP ? Subscribing to GROUP_MESSAGE_SENT - constructor() - GroupChatModulePanel.java");
        EventBus.getInstance().subscribe("GROUP_MESSAGE_SENT", data -> {
            if (data instanceof GroupMessage) {
                appendMessage((GroupMessage) data);
            }
        });

        // Listen for external navigation (e.g. from LandingPage)
        DebugLogger.info("GROUP ? Subscribing to OPEN_GROUP_CHAT - constructor() - GroupChatModulePanel.java");
        EventBus.getInstance().subscribe("OPEN_GROUP_CHAT", data -> {
            if (data instanceof Group) {
                this.currentGroup = (Group) data;
                loadGroupHistory();
                cardLayout.show(container, "CHAT");
                startPolling();
            }
        });

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing GroupChatModulePanel after login for user: " + user.getId());
                refreshGroups();
                cardLayout.show(container, "LIST");
            }
        });
    }

    private JPanel createGroupListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        UIUtils.stylePanel(panel);
        
        JLabel title = new JLabel("  My Study Groups");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        panel.add(title, BorderLayout.NORTH);
        
        groupListPanel = new JPanel();
        groupListPanel.setLayout(new BoxLayout(groupListPanel, BoxLayout.Y_AXIS));
        UIUtils.stylePanel(groupListPanel);
        
        panel.add(new JScrollPane(groupListPanel), BorderLayout.CENTER);
        
        refreshGroups();
        
        return panel;
    }

    private JPanel createGroupChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        UIUtils.stylePanel(panel);
        panel.setBackground(Color.decode("#e5ddd5")); // WhatsApp-like background

        // Group Chat Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        leftHeader.setOpaque(false);

        JButton backBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        backBtn.setIcon(shared.Icons.get("back"));
        backBtn.setPreferredSize(new Dimension(55, 45));
        backBtn.addActionListener(e -> {
            stopPolling();
            cardLayout.show(container, "LIST");
        });
        leftHeader.add(backBtn);
        
        JLabel nameLabel = new JLabel("Study Group");
        nameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_NORMAL));
        leftHeader.add(nameLabel);
        header.add(leftHeader, BorderLayout.WEST);
        
        JButton membersBtn = UIUtils.createRoundedButton("Members", UIUtils.PRIMARY_COLOR, Color.WHITE);
        membersBtn.setPreferredSize(new Dimension(140, UIUtils.BUTTON_HEIGHT));
        membersBtn.addActionListener(e -> showGroupMembers());
        header.add(membersBtn, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);

        // Chat History
        chatHistory = new JPanel();
        chatHistory.setLayout(new BoxLayout(chatHistory, BoxLayout.Y_AXIS));
        chatHistory.setBackground(Color.decode("#e5ddd5"));
        
        scrollPane = new JScrollPane(chatHistory);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Input Bar
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(Color.WHITE);
        inputBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        JButton emojiBtn = new JButton(shared.Icons.get("emoji"));
        emojiBtn.setBorderPainted(false);
        emojiBtn.setContentAreaFilled(false);
        emojiBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        emojiBtn.addActionListener(e -> showEmojiPicker());
        inputBar.add(emojiBtn, BorderLayout.WEST);

        JTextField inputField = UIUtils.createModernTextField("Type a message...");
        inputField.setPreferredSize(new Dimension(0, UIUtils.INPUT_HEIGHT));
        inputBar.add(inputField, BorderLayout.CENTER);

        JButton sendBtn = UIUtils.createRoundedButton("", UIUtils.PRIMARY_COLOR, Color.WHITE);
        sendBtn.setIcon(shared.Icons.get("send"));
        sendBtn.setPreferredSize(new Dimension(60, UIUtils.BUTTON_HEIGHT));
        sendBtn.addActionListener(e -> {
            String content = inputField.getText().trim();
            if (!content.isEmpty() && currentGroup != null) {
                User self = SessionManager.getInstance().getCurrentUser();
                if (self != null) {
                    GroupMessage msg = new GroupMessage(0, currentGroup.getId(), self.getUserId(), self.getFullName(), content, "TEXT", "SENT", null, 0);
                    if (groupChatService.sendGroupMessage(msg)) {
                        inputField.setText("");
                    }
                }
            }
        });
        inputBar.add(sendBtn, BorderLayout.EAST);
        
        panel.add(inputBar, BorderLayout.SOUTH);
        
        return panel;
    }

    private void showGroupMembers() {
        if (currentGroup == null) return;
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Group Members: " + currentGroup.getName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);

        DefaultListModel<User> model = new DefaultListModel<>();
        JList<User> list = new JList<>(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    label.setIcon(shared.Icons.get("user_avatar"));
                    label.setText(((User) value).getFullName());
                    label.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
                    label.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                }
                return label;
            }
        });

        new Thread(() -> {
            List<User> members = groupChatService.getGroupMembers(currentGroup.getId());
            SwingUtilities.invokeLater(() -> {
                for (User m : members) model.addElement(m);
            });
        }).start();

        dialog.add(new JScrollPane(list), BorderLayout.CENTER);
        
        JButton closeBtn = UIUtils.createRoundedButton("Close", UIUtils.PRIMARY_COLOR, Color.WHITE);
        closeBtn.setPreferredSize(new Dimension(160, UIUtils.BUTTON_HEIGHT));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bp = new JPanel();
        bp.add(closeBtn);
        dialog.add(bp, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void showEmojiPicker() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Emoji", true);
        dialog.setLayout(new GridLayout(4, 4, 15, 15));
        String[] emojis = {"😊", "😂", "❤️", "👍", "🔥", "🙌", "😭", "😮", "🙏", "🎉", "✨", "🚀"};
        
        for (String e : emojis) {
            JButton btn = new JButton(e);
            btn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 32));
            btn.addActionListener(evt -> {
                User self = SessionManager.getInstance().getCurrentUser();
                if (currentGroup != null && self != null) {
                    GroupMessage msg = new GroupMessage(0, currentGroup.getId(), self.getUserId(), self.getFullName(), e, "EMOJI", "SENT", null, 0);
                    groupChatService.sendGroupMessage(msg);
                }
                dialog.dispose();
            });
            dialog.add(btn);
        }
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void loadGroupHistory() {
        if (currentGroup == null) return;
        
        chatHistory.removeAll();
        lastLoadedMessageId = 0;
        List<GroupMessage> history = groupChatService.getGroupHistory(currentGroup.getId());
        for (GroupMessage msg : history) {
            appendMessage(msg);
            if (msg.getMessageId() > lastLoadedMessageId) {
                lastLoadedMessageId = msg.getMessageId();
            }
        }
        revalidate();
        repaint();
        scrollToBottom();
    }

    private void appendMessage(GroupMessage msg) {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null || currentGroup == null || msg.getGroupId() != currentGroup.getId()) return;

        boolean isMe = msg.getSenderId() == self.getUserId();
        JPanel bubble = createGroupBubble(msg, isMe);
        chatHistory.add(bubble);
        chatHistory.add(Box.createVerticalStrut(5));
        
        if (isMe) {
            AnimationUtils.fadeIn(bubble, 300);
        } else {
            AnimationUtils.slideIn(bubble, -50, 0, 300);
        }
        
        chatHistory.revalidate();
        chatHistory.repaint();
        scrollToBottom();
    }

    private JPanel createGroupBubble(GroupMessage msg, boolean isMe) {
        UIUtils.FadePanel wrapper = new UIUtils.FadePanel();
        wrapper.setLayout(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        JPanel bubble = new JPanel(new BorderLayout(5, 5));
        bubble.setOpaque(true);
        bubble.setBackground(isMe ? UIUtils.SENT_BUBBLE_COLOR : UIUtils.RECEIVED_BUBBLE_COLOR);
        bubble.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 30), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        if (!isMe) {
            JLabel senderLabel = new JLabel(msg.getSenderName() != null ? msg.getSenderName() : "Unknown");
            senderLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
            senderLabel.setForeground(UIUtils.PRIMARY_COLOR);
            bubble.add(senderLabel, BorderLayout.NORTH);
        }

        JLabel msgLabel = new JLabel("<html><body style='width: 300px'>" + msg.getContent() + "</body></html>");
        msgLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        bubble.add(msgLabel, BorderLayout.CENTER);

        String timeStr = "Just now";
        if (msg.getTimestamp() != null) {
            timeStr = new java.text.SimpleDateFormat("hh:mm a").format(msg.getTimestamp());
        }
        JLabel time = new JLabel(timeStr);
        time.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 10));
        time.setForeground(Color.GRAY);
        bubble.add(time, BorderLayout.SOUTH);

        wrapper.add(bubble);
        return wrapper;
    }

    private void startPolling() {
        stopPolling();
        pollingTimer = new Timer(3000, e -> {
            if (currentGroup != null) {
                List<GroupMessage> newMessages = groupChatService.getGroupHistory(currentGroup.getId());
                for (GroupMessage m : newMessages) {
                    if (m.getMessageId() > lastLoadedMessageId) {
                        appendMessage(m);
                        lastLoadedMessageId = m.getMessageId();
                    }
                }
            }
        });
        pollingTimer.start();
    }

    private void stopPolling() {
        if (pollingTimer != null) {
            pollingTimer.stop();
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private JPanel createCreateGroupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        UIUtils.stylePanel(panel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Create New Group");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        panel.add(title, gbc);

        gbc.gridy++;
        JTextField nameField = UIUtils.createModernTextField("Group Name");
        nameField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        panel.add(nameField, gbc);

        gbc.gridy++;
        JTextField descField = UIUtils.createModernTextField("Group Description");
        descField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        panel.add(descField, gbc);

        gbc.gridy++;
        JLabel membersLabel = new JLabel("Select Members (Contacts):");
        membersLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_NORMAL));
        panel.add(membersLabel, gbc);

        gbc.gridy++;
        DefaultListModel<User> listModel = new DefaultListModel<>();
        JList<User> contactList = new JList<>(listModel);
        contactList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    label.setIcon(shared.Icons.get("user_avatar"));
                    label.setText(((User) value).getFullName());
                    label.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
                    label.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                }
                return label;
            }
        });
        contactList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scroll = new JScrollPane(contactList);
        scroll.setPreferredSize(new Dimension(400, 200));
        panel.add(scroll, gbc);

        // Load contacts for selection
        User self = SessionManager.getInstance().getCurrentUser();
        if (self != null) {
            new Thread(() -> {
                List<User> contacts = new teams.team3_contacts.ContactsDAO().getAcceptedContacts(self.getUserId());
                SwingUtilities.invokeLater(() -> {
                    for (User c : contacts) listModel.addElement(c);
                });
            }).start();
        }

        gbc.gridy++;
        JButton createBtn = UIUtils.createRoundedButton("Create", UIUtils.PRIMARY_COLOR, Color.WHITE);
        createBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        createBtn.setEnabled(false);

        // Validation
        javax.swing.event.DocumentListener groupValidator = new javax.swing.event.DocumentListener() {
            private void validate() {
                createBtn.setEnabled(nameField.getText().trim().length() >= 3);
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { validate(); }
        };
        nameField.getDocument().addDocumentListener(groupValidator);

        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            User currentUser = SessionManager.getInstance().getCurrentUser();
            
            if (currentUser != null) {
                List<Integer> selectedMemberIds = new ArrayList<>();
                for (User selected : contactList.getSelectedValuesList()) {
                    selectedMemberIds.add(selected.getUserId());
                }
                
                if (groupChatService.createGroup(name, desc, selectedMemberIds)) {
                    UIUtils.showToast("Group Created!", this);
                    cardLayout.show(container, "LIST");
                } else {
                    UIUtils.showToast("Failed to create group", this);
                }
            }
        });
        panel.add(createBtn, gbc);

        return panel;
    }

    private void refreshGroups() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        new Thread(() -> {
            List<Group> groups = groupChatService.getUserGroups();
            SwingUtilities.invokeLater(() -> {
                groupListPanel.removeAll();
                for (Group g : groups) {
                    groupListPanel.add(createGroupCard(g));
                }
                groupListPanel.revalidate();
                groupListPanel.repaint();
            });
        }).start();
    }

    private JPanel createGroupCard(Group group) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#e6e6e6")),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel nameLabel = new JLabel(group.getName());
        nameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        JLabel descLabel = new JLabel(group.getDescription());
        descLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.CAPTION));
        descLabel.setForeground(Color.GRAY);
        info.add(nameLabel);
        info.add(descLabel);
        card.add(info, BorderLayout.CENTER);

        JButton openBtn = UIUtils.createRoundedButton("Open", UIUtils.PRIMARY_COLOR, Color.WHITE);
        openBtn.setPreferredSize(new Dimension(100, 45));
        openBtn.addActionListener(e -> {
            this.currentGroup = group;
            loadGroupHistory();
            cardLayout.show(container, "CHAT");
            startPolling();
        });
        card.add(openBtn, BorderLayout.EAST);

        return card;
    }
}

