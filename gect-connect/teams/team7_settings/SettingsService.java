package teams.team7_settings;

import core.UserSettings;
import shared.EventBus;
import shared.DebugLogger;
import shared.SessionManager;

/**
 * Service for managing user settings.
 * Refactored to use SessionManager for current user context.
 */
public class SettingsService {
    private final SettingsDAO settingsDAO;

    public SettingsService() {
        DebugLogger.info("SETTINGS ? Initializing service - constructor() - SettingsService.java");
        this.settingsDAO = new SettingsDAO();
    }

    /**
     * Get settings for current user.
     */
    public UserSettings getSettings() {
        if (!SessionManager.getInstance().isLoggedIn()) return new UserSettings(0, "LIGHT", "PUBLIC", true);
        return settingsDAO.getSettings();
    }

    /**
     * Get settings for specific user.
     */
    public UserSettings getSettings(int userId) {
        DebugLogger.info("SETTINGS ? Fetching settings for user " + userId + " - getSettings() - SettingsService.java");
        return settingsDAO.getSettings(userId);
    }

    /**
     * Update settings for current or specified user.
     */
    public boolean updateSettings(UserSettings settings) {
        if (settings == null) return false;
        
        // Automatically set userId if not present
        if (settings.getUserId() <= 0) {
            settings.setUserId(SessionManager.getInstance().getCurrentUserId());
        }

        DebugLogger.info("SETTINGS ? Updating settings for user " + settings.getUserId() + " - updateSettings() - SettingsService.java");
        boolean success = settingsDAO.updateSettings(settings);
        if (success) {
            DebugLogger.info("SETTINGS ? Settings update SUCCESS - updateSettings() - SettingsService.java");
            EventBus.getInstance().publish("SETTINGS_UPDATED", settings);
        } else {
            DebugLogger.error("SETTINGS ? Settings update FAILED - updateSettings() - SettingsService.java");
        }
        return success;
    }
    
    /**
     * Toggle theme for current user.
     */
    public boolean toggleTheme(boolean isDark) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) return false;
        return toggleTheme(userId, isDark);
    }

    /**
     * Toggle theme for specific user.
     */
    public boolean toggleTheme(int userId, boolean isDark) {
        DebugLogger.info("SETTINGS ? Toggling theme for user " + userId + " (Dark: " + isDark + ") - toggleTheme() - SettingsService.java");
        UserSettings settings = getSettings(userId);
        settings.setTheme(isDark ? "DARK" : "LIGHT");
        return updateSettings(settings);
    }
}
