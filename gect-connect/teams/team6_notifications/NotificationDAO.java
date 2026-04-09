package teams.team6_notifications;

import core.Notification;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Notifications module, aligned with the new schema.
 * Refactored to use SessionManager for current user context.
 */
public class NotificationDAO {

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
            DebugLogger.error("NOTIFICATIONS ? ERROR: Operation failed because no user is currently logged in.");
        }
        return userId;
    }

    /**
     * Create a notification for the current user.
     */
    public boolean createNotification(String title, String content) {
        return createNotification(getRequiredCurrentUserId(), title, content);
    }

    /**
     * Create a notification for the specified user.
     */
    public boolean createNotification(int userId, String title, String content) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        DebugLogger.info("NOTIFICATIONS ? Entering createNotification - createNotification() - NotificationDAO.java");
        String sql = "INSERT INTO notifications (user_id, message, type) VALUES (?, ?, ?)";
        DebugLogger.info("NOTIFICATIONS ? [INSERT] " + sql + " - createNotification() - NotificationDAO.java");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, content);
            pstmt.setString(3, "GENERAL"); // Using GENERAL as default type for now
            boolean result = pstmt.executeUpdate() > 0;
            DebugLogger.info("NOTIFICATIONS ? Exiting createNotification - createNotification() - NotificationDAO.java");
            return result;
        } catch (SQLException e) {
            DebugLogger.error("NOTIFICATIONS ? ERROR in createNotification: " + e.getMessage() + " - createNotification() - NotificationDAO.java");
            return false;
        }
    }

    /**
     * Get notifications for the current user.
     */
    public List<Notification> getNotificationsForUser() {
        return getNotificationsForUser(getRequiredCurrentUserId());
    }

    /**
     * Get notifications for the specified user.
     */
    public List<Notification> getNotificationsForUser(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        DebugLogger.info("NOTIFICATIONS ? Entering getNotificationsForUser - getNotificationsForUser() - NotificationDAO.java");
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        DebugLogger.info("NOTIFICATIONS ? [SELECT] " + sql + " - getNotificationsForUser() - NotificationDAO.java");
        System.out.println("[DB DEBUG] Executing query in NotificationDAO.getNotificationsForUser: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getString("type"),
                        rs.getBoolean("is_read"),
                        rs.getTimestamp("created_at")
                    ));
                }
                System.out.println("[DB DEBUG] Query result: " + notifications.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("NOTIFICATIONS ? ERROR in getNotificationsForUser: " + e.getMessage() + " - getNotificationsForUser() - NotificationDAO.java");
        }
        DebugLogger.info("NOTIFICATIONS ? Exiting getNotificationsForUser - getNotificationsForUser() - NotificationDAO.java");
        return notifications;
    }

    /**
     * Legacy method for current user.
     */
    public List<Notification> getNotifications() {
        return getNotificationsForUser(getRequiredCurrentUserId());
    }

    /**
     * Legacy method for compatibility.
     */
    public List<Notification> getNotifications(int userId) {
        return getNotificationsForUser(userId);
    }

    /**
     * Compatibility method.
     */
    public List<Notification> getNotifications(int userId, String userDepartment) {
        return getNotifications(userId);
    }

    /**
     * Mark a specific notification as read.
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("NOTIFICATIONS ? ERROR in markAsRead: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mark all notifications for current user as read.
     */
    public boolean markAllAsRead() {
        return markAllAsRead(getRequiredCurrentUserId());
    }

    /**
     * Mark all notifications for a user as read.
     */
    public boolean markAllAsRead(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("NOTIFICATIONS ? ERROR in markAllAsRead: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the count of unread notifications for current user.
     */
    public int getUnreadCount() {
        return getUnreadCount(getRequiredCurrentUserId());
    }

    /**
     * Get the count of unread notifications for a user.
     */
    public int getUnreadCount(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return 0;

        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            DebugLogger.error("NOTIFICATIONS ? ERROR in getUnreadCount: " + e.getMessage());
        }
        return 0;
    }
}
