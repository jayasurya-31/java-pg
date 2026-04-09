package core;

/**
 * Model for User Settings in GECT Connect.
 */
public class UserSettings {
    private int userId;
    private String theme; // 'LIGHT', 'DARK'
    private String privacy; // 'PUBLIC', 'PRIVATE', 'CONTACTS'
    private boolean notificationsEnabled;

    public UserSettings(int userId, String theme, String privacy, boolean notificationsEnabled) {
        this.userId = userId;
        this.theme = theme;
        this.privacy = privacy;
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getUserId() { return userId; }
    public String getTheme() { return theme; }
    public String getPrivacy() { return privacy; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setTheme(String theme) { this.theme = theme; }
    public void setPrivacy(String privacy) { this.privacy = privacy; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
}
