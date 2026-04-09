package core;

import java.sql.Timestamp;

/**
 * Model for Notifications in GECT Connect.
 */
public class Notification {
    private int id;
    private int userId;
    private String message;
    private String type;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification(int id, int userId, String message, String type, boolean isRead, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setRead(boolean read) { isRead = read; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
