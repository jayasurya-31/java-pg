package teams.team3_contacts;

import core.User;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Contact Management Module.
 * Handles all database CRUD operations for contacts, requests, and blocks.
 * Refactored to use SessionManager for current user context.
 */
public class ContactsDAO {

    /**
     * Helper to get current user ID from session.
     * @return userId if logged in, -1 otherwise.
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
            DebugLogger.error("CONTACTS → ERROR: Operation failed because no user is currently logged in.");
        }
        return userId;
    }

    /**
     * Search for users with their relationship status.
     * @deprecated Use getUsersWithStatus instead.
     */
    @Deprecated
    public List<User> searchUsers(String query) {
        return searchUsers(query, getRequiredCurrentUserId());
    }

    /**
     * Search for users with their relationship status.
     * @deprecated Use getUsersWithStatus instead.
     */
    @Deprecated
    public List<User> searchUsers(String query, int currentUserId) {
        if (currentUserId <= 0) currentUserId = getCurrentUserIdFromSession();
        if (currentUserId <= 0) return new ArrayList<>();

        List<User> results = new ArrayList<>();
        String sql = "SELECT id, email, full_name, department, roll_no_emp_id, profile_pic FROM users " +
                     "WHERE (full_name LIKE ? OR email LIKE ? OR department LIKE ? OR roll_no_emp_id LIKE ?) " +
                     "AND id != ? " +
                     "AND id NOT IN (SELECT user_id FROM blocked_users WHERE blocked_user_id = ?) " +
                     "AND id NOT IN (SELECT blocked_user_id FROM blocked_users WHERE user_id = ?)";
        
        System.out.println("[DB DEBUG] Executing query in ContactsDAO.searchUsers: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + query + "%";
            for (int i = 1; i <= 4; i++) pstmt.setString(i, searchPattern);
            pstmt.setInt(5, currentUserId);
            pstmt.setInt(6, currentUserId);
            pstmt.setInt(7, currentUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setDepartment(rs.getString("department"));
                    u.setRollNoEmpId(rs.getString("roll_no_emp_id"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    results.add(u);
                }
                System.out.println("[DB DEBUG] Query result: " + results.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in searchUsers: " + e.getMessage());
        }
        return results;
    }

    /**
     * Fetch all users (or filtered by query) with their relationship status to the current user.
     */
    public List<UserRelationship> getUsersWithStatus(String query) {
        return getUsersWithStatus(query, getRequiredCurrentUserId());
    }

    /**
     * Fetch all users (or filtered by query) with their relationship status to the current user.
     */
    public List<UserRelationship> getUsersWithStatus(String query, int currentUserId) {
        if (currentUserId <= 0) currentUserId = getCurrentUserIdFromSession();
        if (currentUserId <= 0) return new ArrayList<>();

        List<UserRelationship> results = new ArrayList<>();
        String sql = "SELECT u.id, u.email, u.full_name, u.profile_pic, u.department, " +
                     "(SELECT status FROM contact_requests " +
                     " WHERE (sender_id = ? AND receiver_id = u.id) " +
                     "    OR (sender_id = u.id AND receiver_id = ?) LIMIT 1) as request_status, " +
                     "(SELECT COUNT(*) FROM blocked_users " +
                     " WHERE (user_id = ? AND blocked_user_id = u.id) " +
                     "    OR (user_id = u.id AND blocked_user_id = ?)) as blocked_count " +
                     "FROM users u " +
                     "WHERE u.id != ? " +
                     "AND u.id NOT IN (SELECT blocked_user_id FROM blocked_users WHERE user_id = ?) " +
                     "AND u.id NOT IN (SELECT user_id FROM blocked_users WHERE blocked_user_id = ?) ";
        
        boolean hasQuery = query != null && !query.trim().isEmpty();
        if (hasQuery) {
            sql += "AND (u.full_name LIKE ? OR u.email LIKE ? OR u.department LIKE ?) ";
        }
        
        sql += "ORDER BY u.full_name ASC";

        System.out.println("[DB DEBUG] Executing query in ContactsDAO.getUsersWithStatus: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, currentUserId);
            pstmt.setInt(3, currentUserId);
            pstmt.setInt(4, currentUserId);
            pstmt.setInt(5, currentUserId);
            pstmt.setInt(6, currentUserId);
            pstmt.setInt(7, currentUserId);

            if (hasQuery) {
                String pattern = "%" + query + "%";
                pstmt.setString(8, pattern);
                pstmt.setString(9, pattern);
                pstmt.setString(10, pattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    u.setDepartment(rs.getString("department"));
                    
                    String status = rs.getString("request_status");
                    boolean blocked = rs.getInt("blocked_count") > 0;
                    
                    results.add(new UserRelationship(u, status, blocked));
                }
                System.out.println("[DB DEBUG] Query result: " + results.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in getUsersWithStatus: " + e.getMessage());
        }
        return results;
    }

    /**
     * Send a contact request.
     */
    public boolean sendRequest(int receiverId) {
        return sendRequest(getRequiredCurrentUserId(), receiverId);
    }

    /**
     * Send a contact request. Status defaults to 'PENDING'.
     * Handles existing requests by updating them if necessary.
     */
    public boolean sendRequest(int senderId, int receiverId) {
        if (senderId <= 0) senderId = getCurrentUserIdFromSession();
        if (senderId <= 0) return false;

        String checkSql = "SELECT status FROM contact_requests WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        String insertSql = "INSERT INTO contact_requests (sender_id, receiver_id, status) VALUES (?, ?, 'PENDING')";
        String updateSql = "UPDATE contact_requests SET status = 'PENDING', sender_id = ?, receiver_id = ? WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, senderId); checkStmt.setInt(2, receiverId);
                checkStmt.setInt(3, receiverId); checkStmt.setInt(4, senderId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentStatus = rs.getString("status");
                        if ("PENDING".equalsIgnoreCase(currentStatus) || "ACCEPTED".equalsIgnoreCase(currentStatus)) {
                            return false; 
                        }
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, senderId); updateStmt.setInt(2, receiverId);
                            updateStmt.setInt(3, senderId); updateStmt.setInt(4, receiverId);
                            updateStmt.setInt(5, receiverId); updateStmt.setInt(6, senderId);
                            return updateStmt.executeUpdate() > 0;
                        }
                    }
                }
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, senderId);
                insertStmt.setInt(2, receiverId);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in sendRequest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve received pending requests for the current user.
     */
    public List<User> getReceivedRequests() {
        return getReceivedRequests(getRequiredCurrentUserId());
    }

    /**
     * Retrieve received pending requests for the specified user.
     */
    public List<User> getReceivedRequests(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        List<User> requests = new ArrayList<>();
        String sql = "SELECT u.id, u.email, u.full_name, u.profile_pic FROM users u " +
                     "JOIN contact_requests cr ON u.id = cr.sender_id " +
                     "WHERE cr.receiver_id = ? AND cr.status = 'PENDING'";
        System.out.println("[DB DEBUG] Executing query in ContactsDAO.getReceivedRequests: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    requests.add(u);
                }
                System.out.println("[DB DEBUG] Query result: " + requests.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in getReceivedRequests: " + e.getMessage());
        }
        return requests;
    }

    /**
     * Retrieve sent pending requests by the current user.
     */
    public List<User> getSentRequests() {
        return getSentRequests(getRequiredCurrentUserId());
    }

    /**
     * Retrieve sent pending requests by the specified user.
     */
    public List<User> getSentRequests(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        List<User> requests = new ArrayList<>();
        String sql = "SELECT u.id, u.email, u.full_name, u.profile_pic FROM users u " +
                     "JOIN contact_requests cr ON u.id = cr.receiver_id " +
                     "WHERE cr.sender_id = ? AND cr.status = 'PENDING'";
        System.out.println("[DB DEBUG] Executing query in ContactsDAO.getSentRequests: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    requests.add(u);
                }
                System.out.println("[DB DEBUG] Query result: " + requests.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in getSentRequests: " + e.getMessage());
        }
        return requests;
    }

    /**
     * Remove a pending contact request.
     */
    public boolean deleteRequest(int receiverId) {
        return deleteRequest(getRequiredCurrentUserId(), receiverId);
    }

    /**
     * Remove a pending contact request.
     */
    public boolean deleteRequest(int senderId, int receiverId) {
        if (senderId <= 0) senderId = getCurrentUserIdFromSession();
        if (senderId <= 0) return false;

        String sql = "DELETE FROM contact_requests WHERE sender_id = ? AND receiver_id = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in deleteRequest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancel a sent request (from current user).
     */
    public boolean cancelRequest(int receiverId) {
        return cancelRequest(getRequiredCurrentUserId(), receiverId);
    }

    /**
     * Cancel a sent request (from sender).
     */
    public boolean cancelRequest(int senderId, int receiverId) {
        if (senderId <= 0) senderId = getCurrentUserIdFromSession();
        return deleteRequest(senderId, receiverId);
    }

    /**
     * Reject a received request (for current user).
     */
    public boolean rejectRequest(int senderId) {
        return rejectRequest(senderId, getRequiredCurrentUserId());
    }

    /**
     * Reject a received request (from receiver).
     */
    public boolean rejectRequest(int senderId, int receiverId) {
        if (receiverId <= 0) receiverId = getCurrentUserIdFromSession();
        if (receiverId <= 0) return false;

        String sql = "UPDATE contact_requests SET status = 'REJECTED' WHERE sender_id = ? AND receiver_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in rejectRequest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update contact request status to ACCEPTED for current user.
     */
    public boolean acceptRequest(int senderId) {
        return acceptRequest(senderId, getRequiredCurrentUserId());
    }

    /**
     * Update contact request status to ACCEPTED.
     * Also creates reciprocal entries in the 'contacts' table.
     */
    public boolean acceptRequest(int senderId, int receiverId) {
        if (receiverId <= 0) receiverId = getCurrentUserIdFromSession();
        if (receiverId <= 0) return false;

        String updateSql = "UPDATE contact_requests SET status = 'ACCEPTED' WHERE sender_id = ? AND receiver_id = ?";
        String insertContactSql = "INSERT IGNORE INTO contacts (user_id, contact_id) VALUES (?, ?), (?, ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean success = false;
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setInt(1, senderId);
                    pstmt.setInt(2, receiverId);
                    success = pstmt.executeUpdate() > 0;
                }

                if (success) {
                    try (PreparedStatement pstmt2 = conn.prepareStatement(insertContactSql)) {
                        pstmt2.setInt(1, senderId);
                        pstmt2.setInt(2, receiverId);
                        pstmt2.setInt(3, receiverId);
                        pstmt2.setInt(4, senderId);
                        pstmt2.executeUpdate();
                    }
                }
                conn.commit();
                return success;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in acceptRequest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update contact request status (ACCEPTED/REJECTED).
     * @deprecated Use acceptRequest, rejectRequest, or cancelRequest instead.
     */
    @Deprecated
    public boolean updateRequestStatus(int senderId, int receiverId, String status) {
        if ("ACCEPTED".equalsIgnoreCase(status)) {
            return acceptRequest(senderId, receiverId);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            return rejectRequest(senderId, receiverId);
        }
        return false;
    }

    /**
     * Block a user for current user.
     */
    public boolean blockUser(int blockedUserId) {
        return blockUser(getRequiredCurrentUserId(), blockedUserId);
    }

    /**
     * Block a user and remove any existing requests and contact relationships.
     */
    public boolean blockUser(int userId, int blockedUserId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String blockSql = "INSERT INTO blocked_users (user_id, blocked_user_id) VALUES (?, ?)";
        String deleteReqSql = "DELETE FROM contact_requests WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        String deleteContactSql = "DELETE FROM contacts WHERE (user_id = ? AND contact_id = ?) OR (user_id = ? AND contact_id = ?)";
        
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Add to block list
                try (PreparedStatement pstmt = conn.prepareStatement(blockSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.setInt(2, blockedUserId);
                    pstmt.executeUpdate();
                }
                // Remove pending requests
                try (PreparedStatement pstmt = conn.prepareStatement(deleteReqSql)) {
                    pstmt.setInt(1, userId); pstmt.setInt(2, blockedUserId);
                    pstmt.setInt(3, blockedUserId); pstmt.setInt(4, userId);
                    pstmt.executeUpdate();
                }
                // Remove contact relationships
                try (PreparedStatement pstmt = conn.prepareStatement(deleteContactSql)) {
                    pstmt.setInt(1, userId); pstmt.setInt(2, blockedUserId);
                    pstmt.setInt(3, blockedUserId); pstmt.setInt(4, userId);
                    pstmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                if (e.getErrorCode() == 1062) return true; // Already blocked
                throw e;
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in blockUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a contact request is already pending between current user and another user.
     */
    public boolean isRequestPending(int receiverId) {
        return isRequestPending(getRequiredCurrentUserId(), receiverId);
    }

    /**
     * Check if a contact request is already pending between two users.
     */
    public boolean isRequestPending(int senderId, int receiverId) {
        if (senderId <= 0) senderId = getCurrentUserIdFromSession();
        if (senderId <= 0) return false;

        String sql = "SELECT COUNT(*) FROM contact_requests WHERE " +
                     "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                     "AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setInt(3, receiverId);
            pstmt.setInt(4, senderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in isRequestPending: " + e.getMessage());
        }
        return false;
    }

    /**
     * Unblock a user for current user.
     */
    public boolean unblockUser(int blockedUserId) {
        return unblockUser(getRequiredCurrentUserId(), blockedUserId);
    }

    /**
     * Unblock a user.
     */
    public boolean unblockUser(int userId, int blockedUserId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String sql = "DELETE FROM blocked_users WHERE user_id = ? AND blocked_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, blockedUserId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in unblockUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve a single user by ID.
     */
    public User getUserById(int userId) {
        String sql = "SELECT id, email, full_name, profile_pic, department, status FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    u.setDepartment(rs.getString("department"));
                    u.setStatus(rs.getString("status"));
                    return u;
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in getUserById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve all accepted contacts for current user.
     */
    public List<User> getContacts() {
        return getContacts(getRequiredCurrentUserId());
    }

    /**
     * Retrieve all accepted contacts for the user.
     */
    public List<User> getContacts(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        List<User> contacts = new ArrayList<>();
        String sql = "SELECT u.id, u.email, u.full_name, u.profile_pic, u.department FROM users u " +
                     "JOIN contacts c ON u.id = c.contact_id " +
                     "WHERE c.user_id = ? " +
                     "AND u.id NOT IN (SELECT blocked_user_id FROM blocked_users WHERE user_id = ?)";
        System.out.println("[DB DEBUG] Executing query in ContactsDAO.getContacts: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    u.setDepartment(rs.getString("department"));
                    contacts.add(u);
                }
                System.out.println("[DB DEBUG] Query result: " + contacts.size() + " rows");
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in getContacts: " + e.getMessage());
        }
        return contacts;
    }

    /**
     * Alias for getContacts for compatibility.
     */
    public List<User> getAcceptedContacts(int userId) {
        return getContacts(userId);
    }

    /**
     * Check if a user is a contact of current user.
     */
    public boolean isContact(int contactId) {
        return isContact(getRequiredCurrentUserId(), contactId);
    }

    /**
     * Check if a user is a contact of another user.
     */
    public boolean isContact(int userId, int contactId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String sql = "SELECT COUNT(*) FROM contacts WHERE user_id = ? AND contact_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, contactId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in isContact: " + e.getMessage());
        }
        return false;
    }

    /**
     * Remove a contact relationship for current user.
     */
    public boolean deleteContact(int contactId) {
        return deleteContact(getRequiredCurrentUserId(), contactId);
    }

    /**
     * Remove a contact relationship.
     */
    public boolean deleteContact(int userId, int contactId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return false;

        String deleteReqSql = "DELETE FROM contact_requests WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)";
        String deleteContactSql = "DELETE FROM contacts WHERE (user_id = ? AND contact_id = ?) OR (user_id = ? AND contact_id = ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement p1 = conn.prepareStatement(deleteReqSql)) {
                    p1.setInt(1, userId); p1.setInt(2, contactId);
                    p1.setInt(3, contactId); p1.setInt(4, userId);
                    p1.executeUpdate();
                }
                try (PreparedStatement p2 = conn.prepareStatement(deleteContactSql)) {
                    p2.setInt(1, userId); p2.setInt(2, contactId);
                    p2.setInt(3, contactId); p2.setInt(4, userId);
                    p2.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in deleteContact: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve list of blocked users for current user.
     */
    public List<User> getBlockedUsers() {
        return getBlockedUsers(getRequiredCurrentUserId());
    }

    /**
     * Retrieve list of blocked users.
     */
    public List<User> getBlockedUsers(int userId) {
        if (userId <= 0) userId = getCurrentUserIdFromSession();
        if (userId <= 0) return new ArrayList<>();

        List<User> blocked = new ArrayList<>();
        String sql = "SELECT u.id, u.email, u.full_name, u.profile_pic FROM users u " +
                     "JOIN blocked_users bu ON u.id = bu.blocked_user_id " +
                     "WHERE bu.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User u = new User(rs.getInt("id"), rs.getString("email"), rs.getString("full_name"));
                    u.setProfilePic(rs.getString("profile_pic"));
                    blocked.add(u);
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("CONTACTS → ERROR in getBlockedUsers: " + e.getMessage());
        }
        return blocked;
    }
}
