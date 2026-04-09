package teams.team1_auth;

import core.User;
import java.sql.*;
import shared.DBConnection;
import shared.DebugLogger;
import shared.SessionManager;

/**
 * Data Access Object for Authentication module.
 * Refactored to use SessionManager for current user context.
 */
public class AuthDAO {

    public User authenticate(String email, String password) {
        DebugLogger.info("DB ? Authenticating user: " + email + " - authenticate() - AuthDAO.java");
        String sql = "SELECT id, full_name, person_type, roll_no_emp_id, department, password FROM users WHERE email = ?";
        System.out.println("[DB DEBUG] Executing query in AuthDAO.authenticate: " + sql);
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                DebugLogger.error("DB ? Cannot authenticate: DB Connection is NULL - authenticate() - AuthDAO.java");
                return null;
            }

            DebugLogger.info("DB ? Preparing authentication query - authenticate() - AuthDAO.java");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                DebugLogger.info("DB ? Executing query... - authenticate() - AuthDAO.java");
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    DebugLogger.info("DB ? Query executed - authenticate() - AuthDAO.java");
                    if (rs.next()) {
                        String storedPassword = rs.getString("password");
                        if (password.equals(storedPassword)) {
                            DebugLogger.info("DB ? Password match SUCCESS - authenticate() - AuthDAO.java");
                            User user = new User(
                                rs.getInt("id"),
                                email,
                                rs.getString("full_name")
                            );
                            user.setPersonType(rs.getString("person_type"));
                            user.setRollNoEmpId(rs.getString("roll_no_emp_id"));
                            user.setDepartment(rs.getString("department"));
                            updateOnlineStatus(user.getId(), true);
                            System.out.println("[DB DEBUG] Query result: Authentication SUCCESS for " + email);
                            return user;
                        } else {
                            DebugLogger.warn("DB ? Password mismatch for: " + email + " - authenticate() - AuthDAO.java");
                            System.out.println("[DB DEBUG] Query result: Password mismatch for " + email);
                        }
                    } else {
                        DebugLogger.warn("DB ? User not found: " + email + " - authenticate() - AuthDAO.java");
                        System.out.println("[DB DEBUG] Query result: User not found " + email);
                    }
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("DB ? Error in authenticate: " + e.getMessage() + " - authenticate() - AuthDAO.java");
        }
        return null;
    }

    public boolean register(String email, String fullName, String personType, String rollNoEmpId, String department, String password) {
        DebugLogger.info("DB ? Registering user: " + email + " - register() - AuthDAO.java");
        String sql = "INSERT INTO users (email, full_name, password, person_type, roll_no_emp_id, department) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                DebugLogger.error("DB ? Cannot register: DB Connection NULL - register() - AuthDAO.java");
                return false;
            }

            DebugLogger.info("DB ? Preparing registration query - register() - AuthDAO.java");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, fullName);
                pstmt.setString(3, password);
                pstmt.setString(4, personType.toUpperCase());
                pstmt.setString(5, rollNoEmpId);
                pstmt.setString(6, department); // <-- new

                DebugLogger.info("DB ? Executing update... - register() - AuthDAO.java");
                int rows = pstmt.executeUpdate();
                DebugLogger.info("DB ? Registration result: " + (rows > 0) + " - register() - AuthDAO.java");
                return rows > 0;
            }
        } catch (SQLException e) {
            DebugLogger.error("DB ? Error in register: " + e.getMessage() + " - register() - AuthDAO.java");
            return false;
        }
    }

    // 
    public boolean isEmailExists(String email) {
        DebugLogger.info("DB ? Checking email existence: " + email + " - isEmailExists() - AuthDAO.java");
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                DebugLogger.error("DB ? Cannot check email: DB Connection NULL - isEmailExists() - AuthDAO.java");
                return false;
            }

            DebugLogger.info("DB ? Preparing query - isEmailExists() - AuthDAO.java");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                DebugLogger.info("DB ? Executing query... - isEmailExists() - AuthDAO.java");
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean exists = rs.next();
                    DebugLogger.info("DB ? Email exists: " + exists + " - isEmailExists() - AuthDAO.java");
                    return exists;
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("DB ? Error in isEmailExists: " + e.getMessage() + " - isEmailExists() - AuthDAO.java");
            return false;
        }
    }

    public boolean isRollNoExists(String rollNo) {
        DebugLogger.info("DB ? Checking roll no existence: " + rollNo + " - isRollNoExists() - AuthDAO.java");
        String sql = "SELECT 1 FROM users WHERE roll_no_emp_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                DebugLogger.error("DB ? Cannot check roll no: DB Connection NULL - isRollNoExists() - AuthDAO.java");
                return false;
            }

            DebugLogger.info("DB ? Preparing query - isRollNoExists() - AuthDAO.java");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, rollNo);
                DebugLogger.info("DB ? Executing query... - isRollNoExists() - AuthDAO.java");
                try (ResultSet rs = pstmt.executeQuery()) {
                    boolean exists = rs.next();
                    DebugLogger.info("DB ? Roll no exists: " + exists + " - isRollNoExists() - AuthDAO.java");
                    return exists;
                }
            }
        } catch (SQLException e) {
            DebugLogger.error("DB ? Error in isRollNoExists: " + e.getMessage() + " - isRollNoExists() - AuthDAO.java");
            return false;
        }
    }

    /**
     * Update online status for current user.
     */
    public void updateOnlineStatus(boolean isOnline) {
        int userId = SessionManager.getInstance().getCurrentUserId();
        if (userId > 0) {
            updateOnlineStatus(userId, isOnline);
        }
    }

    public void updateOnlineStatus(int userId, boolean isOnline) {
        String sql = "UPDATE users SET is_online = ?, last_seen = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            DebugLogger.info("DB ? Updating online status: " + isOnline + " for user ID " + userId + " - updateOnlineStatus() - AuthDAO.java");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, isOnline);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            DebugLogger.error("DB ? Error in updateOnlineStatus: " + e.getMessage() + " - updateOnlineStatus() - AuthDAO.java");
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                DebugLogger.error("DB ? Cannot update password: DB Connection NULL - updatePassword() - AuthDAO.java");
                return false;
            }
            DebugLogger.info("DB ? Updating password for: " + email + " - updatePassword() - AuthDAO.java");
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newPassword);
                pstmt.setString(2, email);
                int rows = pstmt.executeUpdate();
                DebugLogger.info("DB ? Password update result: " + (rows > 0) + " - updatePassword() - AuthDAO.java");
                return rows > 0;
            }
        } catch (SQLException e) {
            DebugLogger.error("DB ? Error in updatePassword: " + e.getMessage() + " - updatePassword() - AuthDAO.java");
            return false;
        }
    }
}