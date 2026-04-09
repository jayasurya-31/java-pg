package teams.team7_settings;

import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Settings & Feed module.
 * Refactored to use SessionManager for current user context.
 */
public class FeedDAO {

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
            DebugLogger.error("SETTINGS ? ERROR: Operation failed because no user is currently logged in.");
        }
        return userId;
    }

    /**
     * Get recent feed posts for current user and their contacts.
     */
    public List<String> getFeed() {
        return getFeed(getRequiredCurrentUserId());
    }

    /**
     * Get recent feed posts for a user and their contacts.
     */
    public List<String> getFeed(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        DebugLogger.info("SETTINGS ? Entering getFeed - getFeed() - FeedDAO.java");
        List<String> feed = new ArrayList<>();
        String sql = "SELECT content FROM feed " +
                     "WHERE user_id = ? OR user_id IN (SELECT contact_id FROM contacts WHERE user_id = ?) " +
                     "ORDER BY created_at DESC LIMIT 20";
        DebugLogger.info("SETTINGS ? [SELECT] " + sql + " - getFeed() - FeedDAO.java");
        System.out.println("[DB DEBUG] Executing query in FeedDAO.getFeed: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feed.add(rs.getString("content"));
                }
                System.out.println("[DB DEBUG] Query result: " + feed.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("SETTINGS ? ERROR in getFeed: " + e.getMessage() + " - getFeed() - FeedDAO.java");
        }
        DebugLogger.info("SETTINGS ? Exiting getFeed - getFeed() - FeedDAO.java");
        return feed;
    }

    /**
     * Post to feed for current user.
     */
    public boolean postToFeed(String content, String mediaPath) {
        return postToFeed(getRequiredCurrentUserId(), content, mediaPath);
    }

    /**
     * Post to feed.
     */
    public boolean postToFeed(int userId, String content, String mediaPath) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        DebugLogger.info("SETTINGS ? Entering postToFeed - postToFeed() - FeedDAO.java");
        String sql = "INSERT INTO feed (user_id, content, media_path) VALUES (?, ?, ?)";
        DebugLogger.info("SETTINGS ? [INSERT] " + sql + " - postToFeed() - FeedDAO.java");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, content);
            pstmt.setString(3, mediaPath);
            boolean result = pstmt.executeUpdate() > 0;
            DebugLogger.info("SETTINGS ? Exiting postToFeed - postToFeed() - FeedDAO.java");
            return result;
        } catch (SQLException e) {
            DebugLogger.error("SETTINGS ? ERROR in postToFeed: " + e.getMessage() + " - postToFeed() - FeedDAO.java");
            return false;
        }
    }

    /**
     * Post to feed (compatibility).
     */
    public boolean postToFeed(String content) {
        return postToFeed(getRequiredCurrentUserId(), content, null);
    }
}
