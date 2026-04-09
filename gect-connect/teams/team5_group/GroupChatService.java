package teams.team5_group;

import core.Group;
import core.GroupMessage;
import core.User;
import shared.EventBus;
import shared.DebugLogger;
import shared.SessionManager;
import teams.team3_contacts.ContactsDAO;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for Group Chat module logic.
 * Refactored to use SessionManager for current user context.
 */
public class GroupChatService {
    private final GroupChatDAO groupChatDAO;
    private final ContactsDAO contactsDAO;

    public GroupChatService() {
        DebugLogger.info("GROUP ? Initializing service - constructor() - GroupChatService.java");
        this.groupChatDAO = new GroupChatDAO();
        this.contactsDAO = new ContactsDAO();
    }

    /**
     * Create a new group with members for current user.
     */
    public boolean createGroup(String name, String description, List<Integer> memberIds) {
        int creatorId = SessionManager.getInstance().getCurrentUserId();
        if (creatorId <= 0) return false;
        return createGroup(name, description, creatorId, memberIds);
    }

    /**
     * Create a new group with members.
     * Members must be in the current user's contacts.
     */
    public boolean createGroup(String name, String description, int creatorId, List<Integer> memberIds) {
        DebugLogger.info("GROUP ? Creating group: " + name + " - createGroup() - GroupChatService.java");
        // Validate that all members are contacts of the creator
        for (int memberId : memberIds) {
            if (memberId != creatorId && !contactsDAO.isContact(creatorId, memberId)) {
                DebugLogger.warn("GROUP ? Validation FAILED: Member " + memberId + " is not a contact of " + creatorId + " - createGroup() - GroupChatService.java");
                return false;
            }
        }
        
        int groupId = groupChatDAO.createGroup(name, description, creatorId);
        if (groupId > 0) {
            DebugLogger.info("GROUP ? Group created SUCCESS with ID " + groupId + " - createGroup() - GroupChatService.java");
            // Add creator as Admin
            groupChatDAO.addMember(groupId, creatorId, "Admin");
            // Add other members as Member
            for (int memberId : memberIds) {
                if (memberId != creatorId) {
                    groupChatDAO.addMember(groupId, memberId, "Member");
                }
            }
            EventBus.getInstance().publish("GROUP_CREATED", name);
            return true;
        }
        DebugLogger.error("GROUP ? Group creation FAILED - createGroup() - GroupChatService.java");
        return false;
    }

    /**
     * Add current user to group.
     */
    public boolean addMember(int groupId, String role) {
        return groupChatDAO.addMember(groupId, role);
    }

    /**
     * Add member to group.
     */
    public boolean addMember(int groupId, int userId, String role) {
        DebugLogger.info("GROUP ? Adding user " + userId + " to group " + groupId + " as " + role + " - addMember() - GroupChatService.java");
        return groupChatDAO.addMemberToGroup(groupId, userId, role);
    }

    /**
     * Get all groups for current user.
     */
    public List<Group> getUserGroups() {
        if (!SessionManager.getInstance().isLoggedIn()) return new ArrayList<>();
        return groupChatDAO.getUserGroups();
    }

    /**
     * Get all groups for a user.
     */
    public List<Group> getUserGroups(int userId) {
        DebugLogger.info("GROUP ? Fetching groups for user " + userId + " - getUserGroups() - GroupChatService.java");
        return groupChatDAO.getUserGroups(userId);
    }

    /**
     * Send message to group.
     */
    public boolean sendGroupMessage(GroupMessage msg) {
        DebugLogger.info("GROUP ? Sending message to group " + msg.getGroupId() + " - sendGroupMessage() - GroupChatService.java");
        
        // Automatically set senderId if not present
        if (msg.getSenderId() <= 0) {
            msg.setSenderId(SessionManager.getInstance().getCurrentUserId());
        }

        boolean success = groupChatDAO.sendGroupMessage(msg);
        if (success) {
            DebugLogger.info("GROUP ? Message sent successfully - sendGroupMessage() - GroupChatService.java");
            EventBus.getInstance().publish("GROUP_MESSAGE_SENT", msg);
        } else {
            DebugLogger.error("GROUP ? Message FAILED to send - sendGroupMessage() - GroupChatService.java");
        }
        return success;
    }

    /**
     * Get members of a group.
     */
    public List<User> getGroupMembers(int groupId) {
        DebugLogger.info("GROUP ? Fetching members for group " + groupId + " - getGroupMembers() - GroupChatService.java");
        return groupChatDAO.getGroupMembers(groupId);
    }

    /**
     * Get group history.
     */
    public List<GroupMessage> getGroupHistory(int groupId) {
        DebugLogger.info("GROUP ? Fetching history for group " + groupId + " - getGroupHistory() - GroupChatService.java");
        return groupChatDAO.getGroupHistory(groupId);
    }
}
