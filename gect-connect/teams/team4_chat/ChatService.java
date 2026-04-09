package teams.team4_chat;

import core.Message;
import core.TextMessage;
import core.EmojiMessage;
import core.ChatListItem;
import shared.EventBus;
import shared.DebugLogger;
import shared.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Chat module logic.
 * Implements ChatManager functionality from the IndividualChatModule design.
 * Refactored to use SessionManager for current user context.
 */
public class ChatService {
    private final ChatDAO chatDAO;

    public ChatService() {
        DebugLogger.info("CHAT ? Initializing service - constructor() - ChatService.java");
        this.chatDAO = new ChatDAO();
    }

    /**
     * Get chat list for the current user.
     */
    public List<ChatListItem> getChatList() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return chatDAO.getChatList();
    }

    /**
     * Get chat list for the specified user.
     */
    public List<ChatListItem> getChatList(int userId) {
        return chatDAO.getChatList(userId);
    }

    /**
     * Get chat history between current user and contact.
     */
    public List<Message> getChatHistory(int contactId) {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return chatDAO.getChatHistory(contactId);
    }

    /**
     * Get chat history between two users.
     */
    public List<Message> getChatHistory(int userId, int contactId) {
        return chatDAO.getChatHistory(userId, contactId);
    }

    /**
     * Send a message and publish event for UI update.
     */
    public boolean sendMessage(Message message) {
        DebugLogger.info("CHAT ? Sending message... - sendMessage() - ChatService.java");
        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            DebugLogger.warn("CHAT ? Message content is empty or null - sendMessage() - ChatService.java");
            return false;
        }

        // Automatically set senderId if not present
        if (message.getSenderId() <= 0) {
            message.setSenderId(SessionManager.getInstance().getCurrentUserId());
        }

        boolean success = chatDAO.sendMessage(message);
        if (success) {
            DebugLogger.info("CHAT ? Message sent successfully - sendMessage() - ChatService.java");
            EventBus.getInstance().publish("CHAT_MESSAGE_SENT", message);
        } else {
            DebugLogger.error("CHAT ? Message FAILED to send - sendMessage() - ChatService.java");
        }
        return success;
    }

    /**
     * Update message status.
     */
    public boolean updateMessageStatus(int messageId, String status) {
        DebugLogger.info("CHAT ? Updating status of message " + messageId + " to " + status + " - updateMessageStatus() - ChatService.java");
        return chatDAO.updateMessageStatus(messageId, status);
    }

    /**
     * Delete message (soft delete).
     */
    public boolean deleteMessage(int messageId) {
        DebugLogger.info("CHAT ? Deleting message " + messageId + " - deleteMessage() - ChatService.java");
        boolean success = chatDAO.deleteMessage(messageId);
        if (success) {
            DebugLogger.info("CHAT ? Message deleted SUCCESS - deleteMessage() - ChatService.java");
            EventBus.getInstance().publish("CHAT_MESSAGE_DELETED", messageId);
        } else {
            DebugLogger.error("CHAT ? Message deletion FAILED - deleteMessage() - ChatService.java");
        }
        return success;
    }

    /**
     * Delete chat for current user.
     */
    public boolean deleteChat(int contactId) {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        return chatDAO.deleteChat(contactId);
    }

    /**
     * Delete chat between two users.
     */
    public boolean deleteChat(int userId, int contactId) {
        return chatDAO.deleteChat(userId, contactId);
    }

    /**
     * Mark messages from a contact as read for current user.
     */
    public boolean markAsRead(int contactId) {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        return chatDAO.markMessagesAsRead(contactId);
    }

    /**
     * Mark messages from a contact as read.
     */
    public boolean markAsRead(int userId, int contactId) {
        return chatDAO.markMessagesAsRead(userId, contactId);
    }

    // For backward compatibility
    public boolean sendMessage(int senderId, int receiverId, String content) {
        Message msg = new TextMessage(0, senderId, receiverId, content, "SENT", null, 0, false);
        return sendMessage(msg);
    }

    public List<Message> getMessageHistory(int userId1, int userId2) {
        return getChatHistory(userId1, userId2);
    }
}
