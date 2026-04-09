package core;

import java.sql.Timestamp;

/**
 * EmojiMessage inherits from Message.
 */
public class EmojiMessage extends Message {
    public EmojiMessage(int id, int senderId, int receiverId, String emoji, String status, Timestamp timestamp, int replyTo, boolean isDeleted) {
        super(id, senderId, receiverId, emoji, "EMOJI", status, timestamp, replyTo, isDeleted);
    }

    public void sendEmoji(String emoji) {
        this.content = emoji;
    }
}
