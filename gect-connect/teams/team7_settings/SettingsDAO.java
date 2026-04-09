package teams.team7_settings;

import core.UserSettings;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.sql.*;

/**
 * DAO for user settings, aligned with the 'settings' table.
 * Refactored to use SessionManager for current user context.
 */
public class SettingsDAO {

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
     * Get settings for current user.
     */
    public UserSettings getSettings() {
        return getSettings(getRequiredCurrentUserId());
    }

    /**
     * Get settings for specific user.
     */
    public UserSettings getSettings(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new UserSettings(0, "LIGHT", "PUBLIC", true);

        DebugLogger.info("SETTINGS ? Entering getSettings - getSettings() - SettingsDAO.java");
        String sql = "SELECT * FROM settings WHERE user_id = ?";
        DebugLogger.info("SETTINGS ? [SELECT] " + sql + " - getSettings() - SettingsDAO.java");
        System.out.println("[DB DEBUG] Executing query in SettingsDAO.getSettings: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserSettings settings = new UserSettings(
                        rs.getInt("user_id"),
                        rs.getString("theme"),
                        rs.getString("privacy"),
                        rs.getBoolean("notifications_enabled")
                    );
                    DebugLogger.info("SETTINGS ? Exiting getSettings - getSettings() - SettingsDAO.java");
                    System.out.println("[DB DEBUG] Query result: Settings found for user " + userId);
                    return settings;
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("SETTINGS ? ERROR in getSettings: " + e.getMessage() + " - getSettings() - SettingsDAO.java");
        }
        // If no settings exist, return default
        DebugLogger.info("SETTINGS ? Returning default settings - getSettings() - SettingsDAO.java");
        System.out.println("[DB DEBUG] Query result: No settings found, returning default for user " + userId);
        DebugLogger.info("SETTINGS ? Exiting getSettings - getSettings() - SettingsDAO.java");
        return new UserSettings(userId, "LIGHT", "PUBLIC", true);
    }

    /**
     * Update current user settings.
     */
    public boolean updateSettings(UserSettings settings) {
        if (settings == null) return false;
        if (settings.getUserId() <= 0) {
            settings.setUserId(getCurrentUserIdFromSession());
        }
        if (settings.getUserId() <= 0) return false;

        DebugLogger.info("SETTINGS ? Entering updateSettings - updateSettings() - SettingsDAO.java");
        String sql = "INSERT INTO settings (user_id, theme, privacy, notifications_enabled) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE theme=?, privacy=?, notifications_enabled=?";
        DebugLogger.info("SETTINGS ? [INSERT/UPDATE] " + sql + " - updateSettings() - SettingsDAO.java");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, settings.getUserId());
            pstmt.setString(2, settings.getTheme());
            pstmt.setString(3, settings.getPrivacy());
            pstmt.setBoolean(4, settings.isNotificationsEnabled());
            
            pstmt.setString(5, settings.getTheme());
            pstmt.setString(6, settings.getPrivacy());
            pstmt.setBoolean(7, settings.isNotificationsEnabled());

            boolean result = pstmt.executeUpdate() > 0;
            DebugLogger.info("SETTINGS ? Exiting updateSettings - updateSettings() - SettingsDAO.java");
            return result;
        } catch (SQLException e) {
            DebugLogger.error("SETTINGS ? ERROR in updateSettings: " + e.getMessage() + " - updateSettings() - SettingsDAO.java");
            return false;
        }
    }
}
