package core;

import java.sql.Timestamp;

/**
 * TextMessage inherits from Message.
 */
public class TextMessage extends Message {
    public TextMessage(int id, int senderId, int receiverId, String content, String status, Timestamp timestamp, int replyTo, boolean isDeleted) {
        super(id, senderId, receiverId, content, "TEXT", status, timestamp, replyTo, isDeleted);
    }

    public void sendText(String content) {
        this.content = content;
    }

    public void editText(String content) {
        this.content = content;
    }
}
