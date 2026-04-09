package teams.team3_contacts;

import core.User;

/**
 * DTO to hold user information along with relationship status to the current user.
 */
public class UserRelationship {
    private User user;
    private String requestStatus; // NONE, PENDING, ACCEPTED, REJECTED
    private boolean blocked;      // True if either user has blocked the other

    public UserRelationship(User user, String requestStatus, boolean blocked) {
        this.user = user;
        this.requestStatus = requestStatus != null ? requestStatus : "NONE";
        this.blocked = blocked;
    }

    public User getUser() { return user; }
    public String getRequestStatus() { return requestStatus; }
    public boolean isBlocked() { return blocked; }

    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
