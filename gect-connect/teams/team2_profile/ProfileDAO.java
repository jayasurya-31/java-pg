package teams.team2_profile;

import core.User;
import java.sql.*;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for Profile module, aligned with the current database schema.
 * Refactored to use SessionManager for current user context.
 */
public class ProfileDAO {

    /**
     * Helper to get current user email from session.
     */
    private String getCurrentUserEmail() {
        User user = SessionManager.getInstance().getCurrentUser();
        return (user != null) ? user.getEmail() : null;
    }

    /**
     * Helper to get current user email and log error if not logged in.
     */
    private String getRequiredCurrentUserEmail() {
        String email = getCurrentUserEmail();
        if (email == null) {
            DebugLogger.error("PROFILE → ERROR: Operation failed because no user is currently logged in.");
        }
        return email;
    }

    /**
     * Get profile data for the current user.
     */
    public User getProfile() {
        return getProfile(getRequiredCurrentUserEmail());
    }

    /**
     * Get profile data for a specific user by email.
     */
    public User getProfile(String email) {
        if (email == null) return null;
        DebugLogger.info("PROFILE → Entering getProfile - getProfile() - ProfileDAO.java");
        String sql = "SELECT id, full_name, department, person_type, roll_no_emp_id, mobile, status, profile_pic, last_seen, is_online, created_at FROM users WHERE email = ?";
        System.out.println("[DB DEBUG] Executing query in ProfileDAO.getProfile: " + sql);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getInt("id"), email, rs.getString("full_name"));
                    user.setDepartment(rs.getString("department"));
                    user.setPersonType(rs.getString("person_type"));
                    user.setRollNoEmpId(rs.getString("roll_no_emp_id"));
                    user.setMobile(rs.getString("mobile"));
                    user.setStatus(rs.getString("status"));
                    user.setProfilePic(rs.getString("profile_pic"));
                    user.setLastSeen(rs.getTimestamp("last_seen"));
                    user.setOnline(rs.getBoolean("is_online"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    System.out.println("[DB DEBUG] Query result: Profile found for " + email);
                    return user;
                }
                System.out.println("[DB DEBUG] Query result: Profile NOT found for " + email);
            }
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in getProfile: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create an empty profile for a user if it doesn't exist.
     */
    public boolean createEmptyProfile(String email) {
        if (email == null) return false;
        String sql = "INSERT INTO users (email, full_name, password, person_type, roll_no_emp_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, "New User");
            pstmt.setString(3, "password123");
            pstmt.setString(4, "STUDENT");
            pstmt.setString(5, "TEMP_" + System.currentTimeMillis());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Duplicate entry
                return true;
            }
            DebugLogger.error("PROFILE → ERROR in createEmptyProfile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update current user profile data partially.
     */
    public boolean updateProfile(String fullName, String status, String mobile, String profilePic, String department, String rollNoEmpId) {
        return updateProfile(getRequiredCurrentUserEmail(), fullName, status, mobile, profilePic, department, rollNoEmpId);
    }

    /**
     * Update user profile data partially. Only non-null fields are updated.
     */
    public boolean updateProfile(String email, String fullName, String status, String mobile, String profilePic, String department, String rollNoEmpId) {
        if (email == null) return false;
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        Map<Integer, Object> params = new HashMap<>();
        int index = 1;

        if (fullName != null) { sql.append("full_name = ?, "); params.put(index++, fullName); }
        if (status != null) { sql.append("status = ?, "); params.put(index++, status); }
        if (mobile != null) { sql.append("mobile = ?, "); params.put(index++, mobile); }
        if (profilePic != null) { sql.append("profile_pic = ?, "); params.put(index++, profilePic); }
        if (department != null) { sql.append("department = ?, "); params.put(index++, department); }
        if (rollNoEmpId != null) { sql.append("roll_no_emp_id = ?, "); params.put(index++, rollNoEmpId); }

        // Remove trailing comma and space
        if (params.isEmpty()) return true; 
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE email = ?");
        params.put(index, email);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (Map.Entry<Integer, Object> entry : params.entrySet()) {
                pstmt.setObject(entry.getKey(), entry.getValue());
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in partial updateProfile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update current user status/bio.
     */
    public boolean updateStatus(String status) {
        return updateStatus(getRequiredCurrentUserEmail(), status);
    }

    /**
     * Update user status/bio.
     */
    public boolean updateStatus(String email, String status) {
        if (email == null) return false;
        String sql = "UPDATE users SET status = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in updateStatus: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update current user profile picture path.
     */
    public boolean updateProfilePicture(String profilePicPath) {
        return updateProfilePicture(getRequiredCurrentUserEmail(), profilePicPath);
    }

    /**
     * Update profile picture path.
     */
    public boolean updateProfilePicture(String email, String profilePicPath) {
        if (email == null) return false;
        String sql = "UPDATE users SET profile_pic = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, profilePicPath);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in updateProfilePicture: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get current user statistics.
     */
    public Map<String, Integer> getUserStatistics() {
        return getUserStatistics(getRequiredCurrentUserEmail());
    }

    /**
     * Get user statistics.
     */
    public Map<String, Integer> getUserStatistics(String email) {
        Map<String, Integer> stats = new HashMap<>();
        if (email == null) return stats;
        String sql = "SELECT COUNT(*) as total_logins FROM login_history WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("total_logins", rs.getInt("total_logins"));
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in getUserStatistics: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Update current user password.
     */
    public boolean changePassword(String newPassword) {
        return changePassword(getRequiredCurrentUserEmail(), newPassword);
    }

    /**
     * Update user password.
     */
    public boolean changePassword(String email, String newPassword) {
        if (email == null) return false;
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in changePassword: " + e.getMessage());
            return false;
        }
    }

    /**
     * Store current user document path in database.
     */
    public boolean uploadDocument(String docName, String docPath) {
        return uploadDocument(getRequiredCurrentUserEmail(), docName, docPath);
    }

    /**
     * Store document path in database.
     */
    public boolean uploadDocument(String email, String docName, String docPath) {
        if (email == null) return false;
        String sql = "INSERT INTO user_documents (email, doc_name, doc_path) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, docName);
            pstmt.setString(3, docPath);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in uploadDocument: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve current user documents.
     */
    public Map<String, String> getDocuments() {
        return getDocuments(getRequiredCurrentUserEmail());
    }

    /**
     * Retrieve user documents.
     */
    public Map<String, String> getDocuments(String email) {
        Map<String, String> docs = new HashMap<>();
        if (email == null) return docs;
        String sql = "SELECT doc_name, doc_path FROM user_documents WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    docs.put(rs.getString("doc_name"), rs.getString("doc_path"));
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in getDocuments: " + e.getMessage());
        }
        return docs;
    }

    /**
     * Remove current user document.
     */
    public boolean deleteDocument(String docName) {
        return deleteDocument(getRequiredCurrentUserEmail(), docName);
    }

    /**
     * Remove a document.
     */
    public boolean deleteDocument(String email, String docName) {
        if (email == null) return false;
        String sql = "DELETE FROM user_documents WHERE email = ? AND doc_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, docName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            DebugLogger.error("PROFILE → ERROR in deleteDocument: " + e.getMessage());
            return false;
        }
    }
}
