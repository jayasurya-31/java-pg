package teams.team4_chat;

import core.Message;
import core.TextMessage;
import core.EmojiMessage;
import core.ChatListItem;
import core.User;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Chat module.
 * Aligned with the IndividualChatModule design.
 * Refactored to use SessionManager for current user context.
 */
public class ChatDAO {

    /**
     * Helper to get current user ID from session.
     */
    private int getCurrentUserIdFromSession() {
        return SessionManager.getInstance().getCurrentUserId();
    }

    /**
     * Helper to get current user ID and log error if not logged in.
     */
    private int getRequiredCurrentUserId() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            DebugLogger.error("CHAT ? ERROR: Operation failed because no user is currently logged in.");
        }
        return userId;
    }

    /**
     * Get chat list for the current user.
     */
    public List<ChatListItem> getChatList() {
        return getChatList(getRequiredCurrentUserId());
    }

    /**
     * Get chat list for the specified user.
     */
    public List<ChatListItem> getChatList(int currentUserId) {
        if (currentUserId <= 0) currentUserId = getCurrentUserIdFromSession();
        if (currentUserId <= 0) return new ArrayList<>();

        DebugLogger.info("CHAT ? Entering getChatList - getChatList() - ChatDAO.java");
        List<ChatListItem> chatList = new ArrayList<>();
        String sql = "SELECT u.id, u.full_name, u.email, u.profile_pic, u.status, " +
                     "m.content as last_msg, m.timestamp as last_ts, c.unread_count " +
                     "FROM contacts c " +
                     "JOIN users u ON c.contact_id = u.id " +
                     "LEFT JOIN messages m ON c.last_message_id = m.id " +
                     "WHERE c.user_id = ? " +
                     "ORDER BY m.timestamp DESC";
        DebugLogger.info("CHAT ? [SELECT] " + sql + " - getChatList() - ChatDAO.java");
        System.out.println("[DB DEBUG] Executing query in ChatDAO.getChatList: " + sql);
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User contact = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    contact.setProfilePic(rs.getString("profile_pic"));
                    contact.setStatus(rs.getString("status"));
                    
                    chatList.add(new ChatListItem(
                        contact,
                        rs.getString("last_msg"),
                        rs.getTimestamp("last_ts"),
                        rs.getInt("unread_count")
                    ));
                }
                System.out.println("[DB DEBUG] Query result: " + chatList.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in getChatList: " + e.getMessage() + " - getChatList() - ChatDAO.java");
        }
        DebugLogger.info("CHAT ? Exiting getChatList - getChatList() - ChatDAO.java");
        return chatList;
    }

    /**
     * Get chat history between current user and contact.
     */
    public List<Message> getChatHistory(int contactId) {
        return getChatHistory(getRequiredCurrentUserId(), contactId);
    }

    /**
     * Get chat history between two users from messages table.
     */
    public List<Message> getChatHistory(int currentUserId, int contactId) {
        if (currentUserId <= 0) currentUserId = getCurrentUserIdFromSession();
        if (currentUserId <= 0) return new ArrayList<>();

        DebugLogger.info("CHAT ? Entering getChatHistory - getChatHistory() - ChatDAO.java");
        List<Message> history = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)) AND is_deleted=0 ORDER BY timestamp ASC";
        DebugLogger.info("CHAT ? [SELECT] " + sql + " - getChatHistory() - ChatDAO.java");
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, contactId);
            pstmt.setInt(3, contactId);
            pstmt.setInt(4, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    int id = rs.getInt("id");
                    int sender = rs.getInt("sender_id");
                    int receiver = rs.getInt("receiver_id");
                    String content = rs.getString("content");
                    String status = rs.getString("status");
                    Timestamp ts = rs.getTimestamp("timestamp");
                    int replyTo = rs.getInt("reply_to");
                    boolean deleted = rs.getBoolean("is_deleted");

                    if ("EMOJI".equals(type)) {
                        history.add(new EmojiMessage(id, sender, receiver, content, status, ts, replyTo, deleted));
                    } else {
                        history.add(new TextMessage(id, sender, receiver, content, status, ts, replyTo, deleted));
                    }
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in getChatHistory: " + e.getMessage() + " - getChatHistory() - ChatDAO.java");
        }
        DebugLogger.info("CHAT ? Exiting getChatHistory - getChatHistory() - ChatDAO.java");
        return history;
    }

    /**
     * Save a new message into the messages table and update contacts helper.
     */
    public boolean sendMessage(Message message) {
        if (message == null) return false;
        if (message.getSenderId() <= 0) {
            message.setSenderId(getRequiredCurrentUserId());
        }
        if (message.getSenderId() <= 0) return false;

        DebugLogger.info("CHAT ? Entering sendMessage - sendMessage() - ChatDAO.java");
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, type, timestamp, status, reply_to, is_deleted) VALUES (?, ?, ?, ?, NOW(), 'SENT', ?, 0)";
        DebugLogger.info("CHAT ? [INSERT] " + sql + " - sendMessage() - ChatDAO.java");
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, message.getSenderId());
                pstmt.setInt(2, message.getReceiverId());
                pstmt.setString(3, message.getContent());
                pstmt.setString(4, message.getType());
                if (message.getReplyTo() > 0) {
                    pstmt.setInt(5, message.getReplyTo());
                } else {
                    pstmt.setNull(5, Types.INTEGER);
                }

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            int msgId = rs.getInt(1);
                            // Update the contacts helper table for both users
                            // Update unread count for the receiver only
                            String updateContactsSql = "UPDATE contacts SET last_message_id = ? WHERE (user_id = ? AND contact_id = ?) OR (user_id = ? AND contact_id = ?)";
                            try (PreparedStatement updatePstmt = conn.prepareStatement(updateContactsSql)) {
                                updatePstmt.setInt(1, msgId);
                                updatePstmt.setInt(2, message.getSenderId());
                                updatePstmt.setInt(3, message.getReceiverId());
                                updatePstmt.setInt(4, message.getReceiverId());
                                updatePstmt.setInt(5, message.getSenderId());
                                updatePstmt.executeUpdate();
                            }

                            String incrementUnreadSql = "UPDATE contacts SET unread_count = unread_count + 1 WHERE user_id = ? AND contact_id = ?";
                            try (PreparedStatement incPstmt = conn.prepareStatement(incrementUnreadSql)) {
                                incPstmt.setInt(1, message.getReceiverId());
                                incPstmt.setInt(2, message.getSenderId());
                                incPstmt.executeUpdate();
                            }
                        }
                    }
                }
                
                conn.commit();
                DebugLogger.info("CHAT ? Exiting sendMessage - sendMessage() - ChatDAO.java");
                return affectedRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                DebugLogger.error("CHAT ? ROLLBACK ERROR in sendMessage: " + e.getMessage() + " - sendMessage() - ChatDAO.java");
                throw e;
            }
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in sendMessage: " + e.getMessage() + " - sendMessage() - ChatDAO.java");
            return false;
        }
    }

    /**
     * Mark all messages from a contact as read and reset unread count for current user.
     */
    public boolean markMessagesAsRead(int contactId) {
        return markMessagesAsRead(getRequiredCurrentUserId(), contactId);
    }

    /**
     * Mark all messages from a contact as read and reset unread count.
     */
    public boolean markMessagesAsRead(int userId, int contactId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        DebugLogger.info("CHAT ? Entering markMessagesAsRead - markMessagesAsRead() - ChatDAO.java");
        String updateMsgsSql = "UPDATE messages SET status='READ' WHERE sender_id=? AND receiver_id=? AND status='SENT'";
        String resetUnreadSql = "UPDATE contacts SET unread_count=0 WHERE user_id=? AND contact_id=?";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                DebugLogger.info("CHAT ? [UPDATE] " + updateMsgsSql + " - markMessagesAsRead() - ChatDAO.java");
                try (PreparedStatement pstmt1 = conn.prepareStatement(updateMsgsSql)) {
                    pstmt1.setInt(1, contactId);
                    pstmt1.setInt(2, userId);
                    pstmt1.executeUpdate();
                }
                DebugLogger.info("CHAT ? [UPDATE] " + resetUnreadSql + " - markMessagesAsRead() - ChatDAO.java");
                try (PreparedStatement pstmt2 = conn.prepareStatement(resetUnreadSql)) {
                    pstmt2.setInt(1, userId);
                    pstmt2.setInt(2, contactId);
                    pstmt2.executeUpdate();
                }
                conn.commit();
                DebugLogger.info("CHAT ? Exiting markMessagesAsRead - markMessagesAsRead() - ChatDAO.java");
                return true;
            } catch (SQLException e) {
                conn.rollback();
                DebugLogger.error("CHAT ? ROLLBACK ERROR in markMessagesAsRead: " + e.getMessage() + " - markMessagesAsRead() - ChatDAO.java");
                throw e;
            }
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in markMessagesAsRead: " + e.getMessage() + " - markMessagesAsRead() - ChatDAO.java");
            return false;
        }
    }

    /**
     * Update message status by ID.
     */
    public boolean updateMessageStatus(int messageId, String status) {
        String sql = "UPDATE messages SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in updateMessageStatus: " + e.getMessage());
            return false;
        }
    }

    /**
     * Soft delete a message.
     */
    public boolean deleteMessage(int messageId) {
        String sql = "UPDATE messages SET is_deleted = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in deleteMessage: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete entire chat between current user and contact.
     */
    public boolean deleteChat(int contactId) {
        return deleteChat(getRequiredCurrentUserId(), contactId);
    }

    /**
     * Delete entire chat history between two users.
     */
    public boolean deleteChat(int userId, int contactId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String sql = "UPDATE messages SET is_deleted = 1 WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, contactId);
            pstmt.setInt(3, contactId);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in deleteChat: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get shared media count between two users.
     */
    public int getSharedMediaCount(int userId, int contactId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)) AND type='IMAGE' AND is_deleted=0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, contactId);
            pstmt.setInt(3, contactId);
            pstmt.setInt(4, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in getSharedMediaCount: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Get shared files count between two users.
     */
    public int getSharedFilesCount(int userId, int contactId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)) AND type='FILE' AND is_deleted=0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, contactId);
            pstmt.setInt(3, contactId);
            pstmt.setInt(4, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            DebugLogger.error("CHAT ? ERROR in getSharedFilesCount: " + e.getMessage());
        }
        return 0;
    }
}
