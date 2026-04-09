package core;

import java.sql.Timestamp;

/**
 * Base Message model for GECT Connect.
 * Aligned with the new database schema and OOP design.
 */
public class Message {
    protected int id;
    protected int senderId;
    protected int receiverId;
    protected String content;
    protected String type; // 'TEXT', 'IMAGE', 'FILE', 'EMOJI'
    protected String status; // 'SENT', 'DELIVERED', 'READ'
    protected Timestamp timestamp;
    protected int replyTo;
    protected boolean isDeleted;

    public Message(int id, int senderId, int receiverId, String content, String type, String status, Timestamp timestamp, int replyTo, boolean isDeleted) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.status = status;
        this.timestamp = timestamp;
        this.replyTo = replyTo;
        this.isDeleted = isDeleted;
    }

    // Getters
    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getReplyTo() { return replyTo; }
    public boolean isDeleted() { return isDeleted; }

    // Setters
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }

    // Methods
    public void markAsRead() { this.status = "READ"; }
    public void softDelete() { this.isDeleted = true; }

    // Compatibility methods
    public String getMessageType() { return type; }
    public int getMessageId() { return id; }
    public Timestamp getSentAt() { return timestamp; }
}
