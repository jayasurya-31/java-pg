package teams.team6_notifications;

import core.Notification;
import shared.EventBus;
import shared.DebugLogger;
import shared.SessionManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Notifications & Media module logic.
 * Refactored to use SessionManager for current user context.
 */
public class NotificationService {
    private final NotificationDAO notificationDAO;

    public NotificationService() {
        DebugLogger.info("NOTIFICATIONS ? Initializing service - constructor() - NotificationService.java");
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Notify current user.
     */
    public boolean notify(String title, String content) {
        return notify(SessionManager.getInstance().getCurrentUserId(), title, content);
    }

    /**
     * Notify specified user.
     */
    public boolean notify(int userId, String title, String content) {
        DebugLogger.info("NOTIFICATIONS ? Creating notification for user " + userId + ": " + title + " - notify() - NotificationService.java");
        boolean success = notificationDAO.createNotification(userId, title, content);
        if (success) {
            DebugLogger.info("NOTIFICATIONS ? Notification created SUCCESS - notify() - NotificationService.java");
            EventBus.getInstance().publish("NOTIFICATION_RECEIVED", content);
        } else {
            DebugLogger.error("NOTIFICATIONS ? Notification creation FAILED - notify() - NotificationService.java");
        }
        return success;
    }

    /**
     * Get notifications for current user.
     */
    public List<Notification> getNotifications() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return notificationDAO.getNotificationsForUser();
    }

    /**
     * Get notifications for specified user.
     */
    public List<Notification> getNotifications(int userId) {
        DebugLogger.info("NOTIFICATIONS ? Fetching notifications for " + userId + " - getNotifications() - NotificationService.java");
        return notificationDAO.getNotificationsForUser(userId);
    }

    public boolean markAsRead(int notificationId) {
        DebugLogger.info("NOTIFICATIONS ? Marking notification " + notificationId + " as read - markAsRead() - NotificationService.java");
        return notificationDAO.markAsRead(notificationId);
    }

    /**
     * Mark all notifications for current user as read.
     */
    public boolean markAllAsRead() {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        return notificationDAO.markAllAsRead();
    }

    /**
     * Mark all notifications for specified user as read.
     */
    public boolean markAllAsRead(int userId) {
        DebugLogger.info("NOTIFICATIONS ? Marking all notifications for " + userId + " as read - markAllAsRead() - NotificationService.java");
        return notificationDAO.markAllAsRead(userId);
    }

    /**
     * Get unread count for current user.
     */
    public int getUnreadCount() {
        if (!SessionManager.getInstance().isLoggedIn()) return 0;
        return notificationDAO.getUnreadCount();
    }

    /**
     * Get unread count for specified user.
     */
    public int getUnreadCount(int userId) {
        DebugLogger.info("NOTIFICATIONS ? Fetching unread count for " + userId + " - getUnreadCount() - NotificationService.java");
        return notificationDAO.getUnreadCount(userId);
    }
}
