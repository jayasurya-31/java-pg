package core;

import java.sql.Timestamp;

/**
 * Model for Group Messages in GECT Connect.
 */
public class GroupMessage {
    private final int messageId;
    private final int groupId;
    private int senderId;
    private final String senderName;
    private final String messageContent;
    private final String messageType;
    private final String status;
    private final Timestamp timestamp;
    private final int replyTo;

    public GroupMessage(int messageId, int groupId, int senderId, String senderName, String messageContent, String messageType, String status, Timestamp timestamp, int replyTo) {
        this.messageId = messageId;
        this.groupId = groupId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageContent = messageContent;
        this.messageType = messageType;
        this.status = status;
        this.timestamp = timestamp;
        this.replyTo = replyTo;
    }

    public int getMessageId() { return messageId; }
    public int getGroupId() { return groupId; }
    public int getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessageContent() { return messageContent; }
    public String getMessageType() { return messageType; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getReplyTo() { return replyTo; }
    
    // Setters
    public void setSenderId(int senderId) { this.senderId = senderId; }
    
    // Compatibility methods
    public String getContent() { return messageContent; }
    public String getType() { return messageType; }
}
