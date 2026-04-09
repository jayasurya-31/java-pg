package teams.team4_chat;

import shared.UIUtils;
import shared.SessionManager;
import shared.EventBus;
import shared.AnimationUtils;
import shared.DebugLogger;
import core.Message;
import core.TextMessage;
import core.EmojiMessage;
import core.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Redesigned Chat Module Panel for GECT Connect.
 */
public class ChatModulePanel extends JPanel {
    private final ChatService chatService;
    private JPanel chatHistory;
    private JScrollPane scrollPane;
    private User currentContact;
    private final JPanel container;
    private final CardLayout cardLayout;
    private Timer pollingTimer;
    private int lastLoadedMessageId = 0;

    public ChatModulePanel() {
        DebugLogger.info("CHAT ? Initializing module - constructor() - ChatModulePanel.java");
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        this.chatService = new ChatService();

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        UIUtils.stylePanel(container);

        container.add(createChatListPlaceholder(), "LIST");
        container.add(createChatWindowPanel(), "WINDOW");

        add(container, BorderLayout.CENTER);

        // Listen for message events
        DebugLogger.info("CHAT ? Subscribing to CHAT_MESSAGE_SENT - constructor() - ChatModulePanel.java");
        EventBus.getInstance().subscribe("CHAT_MESSAGE_SENT", data -> {
            if (data instanceof Message) {
                appendMessage((Message) data);
            }
        });

        // Listen for navigation from LandingPage
        DebugLogger.info("CHAT ? Subscribing to OPEN_CHAT_WINDOW - constructor() - ChatModulePanel.java");
        EventBus.getInstance().subscribe("OPEN_CHAT_WINDOW", data -> {
            if (data instanceof User) {
                this.currentContact = (User) data;
                loadChatHistory();
                cardLayout.show(container, "WINDOW");
                startPolling();
            }
        });

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing ChatModulePanel after login for user: " + user.getId());
                cardLayout.show(container, "LIST");
            }
        });
    }

    private void startPolling() {
        if (pollingTimer != null) pollingTimer.stop();
        
        pollingTimer = new Timer(3000, e -> {
            if (currentContact != null) {
                User self = SessionManager.getInstance().getCurrentUser();
                if (self != null) {
                    List<Message> newMessages = chatService.getChatHistory(self.getUserId(), currentContact.getUserId());
                    for (Message m : newMessages) {
                        if (m.getId() > lastLoadedMessageId) {
                            appendMessage(m);
                            lastLoadedMessageId = m.getId();
                        }
                    }
                }
            }
        });
        pollingTimer.start();
    }

    private void showChatInfo() {
        if (currentContact != null) {
            ChatInfoScreen infoScreen = new ChatInfoScreen(currentContact, () -> cardLayout.show(container, "WINDOW"));
            container.add(infoScreen, "INFO");
            cardLayout.show(container, "INFO");
        }
    }

    private JPanel createChatListPlaceholder() {
        JPanel panel = new JPanel(new GridBagLayout());
        UIUtils.stylePanel(panel);
        panel.add(new JLabel("Select a contact to start chatting."));
        return panel;
    }

    private JPanel createChatWindowPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        UIUtils.stylePanel(panel);
        panel.setBackground(Color.decode("#e5ddd5")); // WhatsApp-like background

        // 1. Chat Header
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
            DebugLogger.info("CHAT ? NAV_CHAT_LIST on backBtn - actionPerformed() - ChatModulePanel.java");
            cardLayout.show(container, "LIST");
        });
        leftHeader.add(backBtn);

        JLabel nameLabel = new JLabel("Chatting...");
        nameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_NORMAL));
        leftHeader.add(nameLabel);
        header.add(leftHeader, BorderLayout.WEST);

        // Header Menu
        JButton moreBtn = new JButton(shared.Icons.get("more"));
        moreBtn.setBorderPainted(false);
        moreBtn.setContentAreaFilled(false);
        moreBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPopupMenu headerMenu = new JPopupMenu();
        JMenuItem viewProfileItem = new JMenuItem("View Profile");
        viewProfileItem.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        viewProfileItem.addActionListener(e -> showChatInfo());
        headerMenu.add(viewProfileItem);
        
        JMenuItem deleteChatItem = new JMenuItem("Delete Chat");
        deleteChatItem.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        deleteChatItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this entire chat?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && currentContact != null) {
                User self = SessionManager.getInstance().getCurrentUser();
                if (chatService.deleteChat(self.getUserId(), currentContact.getUserId())) {
                    cardLayout.show(container, "LIST");
                    EventBus.getInstance().publish("CHAT_DELETED", currentContact.getUserId());
                }
            }
        });
        headerMenu.add(deleteChatItem);

        JMenuItem blockUserItem = new JMenuItem("Block User");
        blockUserItem.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        blockUserItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Block this user?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && currentContact != null) {
                if (new teams.team3_contacts.ContactsService().blockUser(currentContact.getUserId())) {
                    cardLayout.show(container, "LIST");
                    UIUtils.showToast("User Blocked", this);
                }
            }
        });
        headerMenu.add(blockUserItem);
        
        moreBtn.addActionListener(e -> headerMenu.show(moreBtn, 0, moreBtn.getHeight()));
        header.add(moreBtn, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // 2. Chat History (Scrollable)
        chatHistory = new JPanel();
        chatHistory.setLayout(new BoxLayout(chatHistory, BoxLayout.Y_AXIS));
        chatHistory.setBackground(Color.decode("#e5ddd5"));
        
        scrollPane = new JScrollPane(chatHistory);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 3. Message Input Bar
        JPanel inputBar = new JPanel(new BorderLayout(10, 0));
        inputBar.setBackground(Color.WHITE);
        inputBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton emojiBtn = new JButton(shared.Icons.get("emoji"));
        emojiBtn.setBorderPainted(false);
        emojiBtn.setContentAreaFilled(false);
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
            if (!content.isEmpty() && currentContact != null) {
                User self = SessionManager.getInstance().getCurrentUser();
                Message msg = new TextMessage(0, self.getUserId(), currentContact.getUserId(), content, "SENT", null, 0, false);
                if (chatService.sendMessage(msg)) {
                    DebugLogger.info("CHAT ? SUCCESS - Sent to " + currentContact.getUserId() + " - sendMessage() - ChatModulePanel.java");
                } else {
                    DebugLogger.error("CHAT ? FAILURE - Failed to send to " + currentContact.getUserId() + " - sendMessage() - ChatModulePanel.java");
                }
                inputField.setText("");
            }
        });
        inputBar.add(sendBtn, BorderLayout.EAST);

        panel.add(inputBar, BorderLayout.SOUTH);

        return panel;
    }

    private void loadChatHistory() {
        chatHistory.removeAll();
        lastLoadedMessageId = 0;
        User self = SessionManager.getInstance().getCurrentUser();
        if (self != null && currentContact != null) {
            // Mark messages as read when opening history
            chatService.markAsRead(self.getUserId(), currentContact.getUserId());
            EventBus.getInstance().publish("CHAT_READ", currentContact.getUserId());

            List<Message> history = chatService.getChatHistory(self.getUserId(), currentContact.getUserId());
            for (Message msg : history) {
                appendMessage(msg);
                if (msg.getId() > lastLoadedMessageId) {
                    lastLoadedMessageId = msg.getId();
                }
            }
        }
        revalidate();
        repaint();
        scrollToBottom();
    }

    private void appendMessage(Message msg) {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        boolean isSelf = msg.getSenderId() == self.getUserId();
        
        // Ensure the message belongs to the current chat window
        if (currentContact != null && 
            (msg.getReceiverId() == currentContact.getUserId() || msg.getSenderId() == currentContact.getUserId())) {
            
            JPanel bubbleWrapper = createChatBubble(msg, isSelf);
            chatHistory.add(bubbleWrapper);
            chatHistory.add(Box.createVerticalStrut(5));
            
            if (isSelf) {
                AnimationUtils.fadeIn(bubbleWrapper, 300);
            } else {
                AnimationUtils.slideIn(bubbleWrapper, -50, 0, 300);
            }
            
            chatHistory.revalidate();
            chatHistory.repaint();
            scrollToBottom();
        }
    }

    private JPanel createChatBubble(Message msg, boolean isSelf) {
        UIUtils.FadePanel wrapper = new UIUtils.FadePanel();
        wrapper.setLayout(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JPanel bubble = new JPanel(new BorderLayout(8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        bubble.setOpaque(false);
        bubble.setBackground(isSelf ? UIUtils.SENT_BUBBLE_COLOR : UIUtils.RECEIVED_BUBBLE_COLOR);
        bubble.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel text = new JLabel("<html><body style='width: 300px; color: " + (isSelf ? "#ffffff" : "#000000") + "'>" + msg.getContent() + "</body></html>");
        text.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        bubble.add(text, BorderLayout.CENTER);

        // Status + Timestamp
        JPanel meta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        meta.setOpaque(false);
        
        String timeStr = "Just now";
        if (msg.getTimestamp() != null) {
            timeStr = new java.text.SimpleDateFormat("hh:mm a").format(msg.getTimestamp());
        }
        JLabel time = new JLabel(timeStr);
        time.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 10));
        time.setForeground(isSelf ? new Color(255, 255, 255, 180) : Color.GRAY);
        meta.add(time);

        if (isSelf) {
            JLabel status = new JLabel("Read");
            status.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
            status.setForeground(msg.getStatus().equalsIgnoreCase("READ") ? new Color(255, 255, 255, 220) : new Color(255, 255, 255, 140));
            meta.add(status);
        }
        bubble.add(meta, BorderLayout.SOUTH);

        // Options on click
        bubble.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showOptionsDialog(msg, wrapper);
                }
            }
        });

        wrapper.add(bubble);
        return wrapper;
    }

    private void showOptionsDialog(Message msg, JPanel wrapper) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete for me");
        deleteItem.addActionListener(e -> {
            if (chatService.deleteMessage(msg.getId())) {
                AnimationUtils.fadeOut(wrapper, 300, () -> {
                    chatHistory.remove(wrapper);
                    chatHistory.revalidate();
                    chatHistory.repaint();
                });
            }
        });
        menu.add(deleteItem);
        menu.add(new JMenuItem("Copy Text"));
        menu.add(new JMenuItem("Reply"));
        menu.show(wrapper, 0, wrapper.getHeight());
    }

    private void showEmojiPicker() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Emoji", true);
        dialog.setLayout(new GridLayout(4, 4, 10, 10));
        String[] emojis = {"😊", "😂", "❤️", "👍", "🔥", "🙌", "😭", "😮", "🙏", "🎉", "✨", "🚀"};
        
        for (String e : emojis) {
            JButton btn = new JButton(e);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            btn.addActionListener(evt -> {
                User self = SessionManager.getInstance().getCurrentUser();
                if (currentContact != null && self != null) {
                    Message msg = new EmojiMessage(0, self.getUserId(), currentContact.getUserId(), e, "SENT", null, 0, false);
                    chatService.sendMessage(msg);
                }
                dialog.dispose();
            });
            dialog.add(btn);
        }
        dialog.setSize(250, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
