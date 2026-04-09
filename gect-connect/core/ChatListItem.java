package core;

import java.sql.Timestamp;

/**
 * Model for an item in the Chat List.
 */
public class ChatListItem {
    private final User contact;
    private final String lastMessageSnippet;
    private final Timestamp timestamp;
    private final int unreadCount;

    public ChatListItem(User contact, String lastMessageSnippet, Timestamp timestamp, int unreadCount) {
        this.contact = contact;
        this.lastMessageSnippet = lastMessageSnippet;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }

    public User getContact() { return contact; }
    public String getLastMessageSnippet() { return lastMessageSnippet; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getUnreadCount() { return unreadCount; }
}
