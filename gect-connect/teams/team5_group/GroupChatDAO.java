package teams.team5_group;

import core.User;
import core.Group;
import core.GroupMessage;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Group Chat module, fully aligned with the final schema.
 * Refactored to use SessionManager for current user context.
 */
public class GroupChatDAO {

    /**
     * Helper to get current user ID from session.
     */
    private int getCurrentUserIdFromSession() {
        return SessionManager.getInstance().getCurrentUserId();
    }

    /**
     * Helper to get current user ID and log error if not logged in.
     */
    private int getRequiredCurrentUserId() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId <= 0) {
            DebugLogger.error("GROUP ? ERROR: Operation failed because no user is currently logged in.");
        }
        return userId;
    }

    /**
     * Create a new group by current user.
     */
    public int createGroup(String name, String description) {
        return createGroup(name, description, getRequiredCurrentUserId());
    }

    /**
     * Create a new group and return its ID.
     */
    public int createGroup(String name, String description, int creatorId) {
        if (creatorId <= 0) creatorId = getCurrentUserIdFromSession();
        if (creatorId <= 0) return -1;

        DebugLogger.info("GROUP ? Entering createGroup - createGroup() - GroupChatDAO.java");
        String sql = "INSERT INTO groups (name, description, creator_id) VALUES (?, ?, ?)";
        DebugLogger.info("GROUP ? [INSERT] " + sql + " - createGroup() - GroupChatDAO.java");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, creatorId);
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int groupId = rs.getInt(1);
                        DebugLogger.info("GROUP ? Exiting createGroup - createGroup() - GroupChatDAO.java");
                        return groupId;
                    }
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("GROUP ? ERROR in createGroup: " + e.getMessage() + " - createGroup() - GroupChatDAO.java");
        }
        DebugLogger.info("GROUP ? Exiting createGroup - createGroup() - GroupChatDAO.java");
        return -1;
    }

    /**
     * Get members of a group.
     */
    public List<User> getGroupMembers(int groupId) {
        DebugLogger.info("GROUP ? Entering getGroupMembers - getGroupMembers() - GroupChatDAO.java");
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.id, u.email, u.full_name FROM users u " +
                     "JOIN group_members gm ON u.id = gm.user_id " +
                     "WHERE gm.group_id = ?";
        DebugLogger.info("GROUP ? [SELECT] " + sql + " - getGroupMembers() - GroupChatDAO.java");
        System.out.println("[DB DEBUG] Executing query in GroupChatDAO.getGroupMembers: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name")));
                }
                System.out.println("[DB DEBUG] Query result: " + members.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("GROUP ? ERROR in getGroupMembers: " + e.getMessage() + " - getGroupMembers() - GroupChatDAO.java");
        }
        DebugLogger.info("GROUP ? Exiting getGroupMembers - getGroupMembers() - GroupChatDAO.java");
        return members;
    }

    /**
     * Add current user to a group with a specific role.
     */
    public boolean addMember(int groupId, String role) {
        return addMember(groupId, getRequiredCurrentUserId(), role);
    }

    /**
     * Add a member to a group with a specific role.
     */
    public boolean addMember(int groupId, int userId, String role) {
        return addMemberToGroup(groupId, userId, role);
    }

    /**
     * Add a member to a group with a specific role (Legacy name).
     */
    public boolean addMemberToGroup(int groupId, int userId, String role) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String sql = "INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, groupId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, role);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("GROUP ? ERROR in addMemberToGroup: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all groups current user belongs to.
     */
    public List<Group> getUserGroups() {
        return getUserGroups(getRequiredCurrentUserId());
    }

    /**
     * Get all groups a user belongs to.
     */
    public List<Group> getUserGroups(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g " +
                     "JOIN group_members gm ON g.id = gm.group_id " +
                     "WHERE gm.user_id = ?";
        System.out.println("[DB DEBUG] Executing query in GroupChatDAO.getUserGroups: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    groups.add(new Group(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("creator_id"),
                        rs.getTimestamp("created_at")
                    ));
                }
                System.out.println("[DB DEBUG] Query result: " + groups.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("GROUP ? ERROR in getUserGroups: " + e.getMessage());
        }
        return groups;
    }

    /**
     * Send a message to a group.
     */
    public boolean sendGroupMessage(GroupMessage msg) {
        if (msg == null) return false;
        if (msg.getSenderId() <= 0) {
            msg.setSenderId(getCurrentUserIdFromSession());
        }

        String sql = "INSERT INTO group_messages (group_id, sender_id, message_content, message_type, status, reply_to) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, msg.getGroupId());
            if (msg.getSenderId() > 0) pstmt.setInt(2, msg.getSenderId()); else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, msg.getContent());
            pstmt.setString(4, msg.getType());
            pstmt.setString(5, msg.getStatus());
            if (msg.getReplyTo() > 0) pstmt.setInt(6, msg.getReplyTo()); else pstmt.setNull(6, Types.INTEGER);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("GROUP ? ERROR in sendGroupMessage: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get chat history for a group.
     */
    public List<GroupMessage> getGroupHistory(int groupId) {
        List<GroupMessage> history = new ArrayList<>();
        String sql = "SELECT gm.*, u.full_name as sender_name FROM group_messages gm " +
                     "LEFT JOIN users u ON gm.sender_id = u.id " +
                     "WHERE gm.group_id = ? ORDER BY gm.timestamp ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new GroupMessage(
                        rs.getInt("message_id"),
                        rs.getInt("group_id"),
                        rs.getInt("sender_id"),
                        rs.getString("sender_name"),
                        rs.getString("message_content"),
                        rs.getString("message_type"),
                        rs.getString("status"),
                        rs.getTimestamp("timestamp"),
                        rs.getInt("reply_to")
                    ));
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("GROUP ? ERROR in getGroupHistory: " + e.getMessage());
        }
        return history;
    }
}
