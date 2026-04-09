package teams.team3_contacts;

import core.User;
import shared.SessionManager;
import shared.EventBus;
import shared.DebugLogger;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Contact Management Module.
 * Implements business logic and coordinates with ContactsDAO.
 * Refactored to use SessionManager for current user context.
 */
public class ContactsService {
    private final ContactsDAO contactsDAO;

    public ContactsService() {
        this.contactsDAO = new ContactsDAO();
    }

    /**
     * Helper to get current user ID with session check.
     */
    private int getCurrentUserId() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            DebugLogger.error("CONTACTS → SERVICE ERROR: Operation failed because no user is currently logged in.");
            return -1;
        }
        return user.getId();
    }

    /**
     * Search for users with broad query.
     * @deprecated Use getUsersWithStatus instead.
     */
    @Deprecated
    public List<User> searchUsers(String query) {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return contactsDAO.searchUsers(query);
    }

    /**
     * Fetch all users with status, optionally filtered by query.
     */
    public List<UserRelationship> getUsersWithStatus(String query) {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return contactsDAO.getUsersWithStatus(query);
    }

    /**
     * Send contact request and notify.
     */
    public boolean sendRequest(int receiverId) {
        int senderId = getCurrentUserId();
        if (senderId == -1 || senderId == receiverId) return false;
        
        try {
            // Prevent sending if already contacts
            if (contactsDAO.isContact(receiverId)) return false;
            
            // Prevent sending if already pending
            if (contactsDAO.isRequestPending(receiverId)) return false;

            boolean success = contactsDAO.sendRequest(receiverId);
            if (success) {
                EventBus.getInstance().publish("CONTACT_REQUEST_UPDATED", receiverId);
            }
            return success;
        } catch (Exception e) {
            DebugLogger.error("CONTACTS → ERROR in sendRequest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get pending requests received by user.
     */
    public List<User> getReceivedRequests() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        System.out.println("[DEBUG] ContactsService.getReceivedRequests() called for userId=" + SessionManager.getInstance().getCurrentUserId());
        List<User> receivedRequests = contactsDAO.getReceivedRequests();
        System.out.println("[DEBUG] getReceivedRequests returned: " + receivedRequests);
        return receivedRequests;
    }

    /**
     * Get pending requests sent by user.
     */
    public List<User> getSentRequests() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        System.out.println("[DEBUG] ContactsService.getSentRequests() called for userId=" + SessionManager.getInstance().getCurrentUserId());
        List<User> sentRequests = contactsDAO.getSentRequests();
        System.out.println("[DEBUG] getSentRequests returned: " + sentRequests);
        return sentRequests;
    }

    /**
     * Cancel a sent contact request.
     */
    public boolean cancelRequest(int receiverId) {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        boolean success = contactsDAO.cancelRequest(receiverId);
        if (success) {
            EventBus.getInstance().publish("CONTACT_REQUEST_UPDATED", getCurrentUserId());
        }
        return success;
    }

    /**
     * Accept a contact request.
     */
    public boolean acceptRequest(int senderId) {
        int receiverId = getCurrentUserId();
        if (receiverId == -1) return false;
        boolean success = contactsDAO.acceptRequest(senderId);
        if (success) {
            EventBus.getInstance().publish("CONTACT_LIST_UPDATED", receiverId);
            EventBus.getInstance().publish("CONTACT_LIST_UPDATED", senderId);
        }
        return success;
    }

    /**
     * Reject a contact request.
     */
    public boolean rejectRequest(int senderId) {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        return contactsDAO.rejectRequest(senderId);
    }

    /**
     * Get active contacts.
     */
    public List<User> getContacts() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        System.out.println("[DEBUG] ContactsService.getContacts() called for userId=" + SessionManager.getInstance().getCurrentUserId());
        List<User> contacts = contactsDAO.getContacts();
        System.out.println("[DEBUG] ContactsService.getContacts() returned: " + contacts);
        return contacts;
    }

    /**
     * Delete contact.
     */
    public boolean deleteContact(int contactId) {
        int userId = getCurrentUserId();
        if (userId == -1) return false;
        boolean success = contactsDAO.deleteContact(contactId);
        if (success) {
            EventBus.getInstance().publish("CONTACT_LIST_UPDATED", userId);
            EventBus.getInstance().publish("CONTACT_LIST_UPDATED", contactId);
        }
        return success;
    }

    /**
     * Block a user.
     */
    public boolean blockUser(int blockedUserId) {
        int userId = getCurrentUserId();
        if (userId == -1) return false;
        boolean success = contactsDAO.blockUser(blockedUserId);
        if (success) {
            EventBus.getInstance().publish("CONTACT_LIST_UPDATED", userId);
            EventBus.getInstance().publish("CONTACT_REQUEST_UPDATED", userId);
            EventBus.getInstance().publish("BLOCKED_LIST_UPDATED", userId);
        }
        return success;
    }

    /**
     * Unblock a user.
     */
    public boolean unblockUser(int blockedUserId) {
        int userId = getCurrentUserId();
        if (userId == -1) return false;
        boolean success = contactsDAO.unblockUser(blockedUserId);
        if (success) {
            EventBus.getInstance().publish("BLOCKED_LIST_UPDATED", userId);
            EventBus.getInstance().publish("CONTACT_REQUEST_UPDATED", userId);
        }
        return success;
    }

    /**
     * Get blocked users.
     */
    public List<User> getBlockedUsers() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return contactsDAO.getBlockedUsers();
    }

    /**
     * Check if users are contacts.
     */
    public boolean isContact(int contactId) {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        return contactsDAO.isContact(contactId);
    }

    /**
     * Check if a request is already pending.
     */
    public boolean isRequestPending(int receiverId) {
        if (!SessionManager.getInstance().isLoggedIn()) return false;
        return contactsDAO.isRequestPending(receiverId);
    }

    /**
     * Provides a direct chat link for an accepted contact.
     */
    public void openChat(int contactId) {
        User contact = contactsDAO.getUserById(contactId);
        if (contact != null) {
            DebugLogger.info("CONTACTS → Opening chat window for user: " + contact.getFullName());
            EventBus.getInstance().publish("OPEN_CHAT_WINDOW", contact);
        }
    }
}
