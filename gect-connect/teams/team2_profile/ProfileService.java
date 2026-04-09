package teams.team2_profile;

import core.User;
import shared.DebugLogger;
import shared.EventBus;
import shared.SessionManager;
import java.util.Map;

/**
 * Service for Profile module logic.
 * Refactored to use SessionManager for current user context.
 */
public class ProfileService {
    private final ProfileDAO profileDAO;

    public ProfileService() {
        DebugLogger.info("PROFILE → Initializing service - constructor() - ProfileService.java");
        this.profileDAO = new ProfileDAO();
    }

    /**
     * Helper to get current user email with session check.
     */
    private String getCurrentUserEmail() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            DebugLogger.error("PROFILE → SERVICE ERROR: Operation failed because no user is currently logged in.");
            return null;
        }
        return user.getEmail();
    }

    /**
     * Retrieve current user profile.
     */
    public User getProfile() {
        return profileDAO.getProfile();
    }

    /**
     * Retrieve user profile by email.
     */
    public User getProfile(String email) {
        return profileDAO.getProfile(email);
    }

    /**
     * Create an empty profile for a user if it doesn't exist.
     */
    public boolean createEmptyProfile(String email) {
        return profileDAO.createEmptyProfile(email);
    }

    /**
     * Update current user profile data.
     */
    public boolean updateProfile(String fullName, String status, String mobile, String profilePicPath, String department, String rollNoEmpId) {
        String email = getCurrentUserEmail();
        if (email == null) return false;
        boolean success = profileDAO.updateProfile(fullName, status, mobile, profilePicPath, department, rollNoEmpId);
        if (success) {
            EventBus.getInstance().publish("PROFILE_UPDATED", email);
        }
        return success;
    }

    /**
     * Update user profile data by email.
     */
    public boolean updateProfile(String email, String fullName, String status, String mobile, String profilePicPath, String department, String rollNoEmpId) {
        boolean success = profileDAO.updateProfile(email, fullName, status, mobile, profilePicPath, department, rollNoEmpId);
        if (success) {
            EventBus.getInstance().publish("PROFILE_UPDATED", email);
        }
        return success;
    }

    /**
     * Update current user status.
     */
    public boolean updateStatus(String status) {
        String email = getCurrentUserEmail();
        if (email == null) return false;
        boolean success = profileDAO.updateStatus(status);
        if (success) {
            EventBus.getInstance().publish("PROFILE_UPDATED", email);
        }
        return success;
    }

    /**
     * Update user status by email.
     */
    public boolean updateStatus(String email, String status) {
        boolean success = profileDAO.updateStatus(email, status);
        if (success) {
            EventBus.getInstance().publish("PROFILE_UPDATED", email);
        }
        return success;
    }

    /**
     * Update current user profile picture.
     */
    public boolean updateProfilePicture(String profilePicPath) {
        String email = getCurrentUserEmail();
        if (email == null) return false;
        boolean success = profileDAO.updateProfilePicture(profilePicPath);
        if (success) {
            EventBus.getInstance().publish("PROFILE_UPDATED", email);
        }
        return success;
    }

    /**
     * Update profile picture by email.
     */
    public boolean updateProfilePicture(String email, String profilePicPath) {
        boolean success = profileDAO.updateProfilePicture(email, profilePicPath);
        if (success) {
            EventBus.getInstance().publish("PROFILE_UPDATED", email);
        }
        return success;
    }

    /**
     * Get current user statistics.
     */
    public Map<String, Integer> getUserStatistics() {
        return profileDAO.getUserStatistics();
    }

    /**
     * Get user statistics by email.
     */
    public Map<String, Integer> getUserStatistics(String email) {
        return profileDAO.getUserStatistics(email);
    }

    /**
     * Update current user password.
     */
    public boolean changePassword(String newPassword) {
        return profileDAO.changePassword(newPassword);
    }

    /**
     * Update user password by email.
     */
    public boolean changePassword(String email, String newPassword) {
        return profileDAO.changePassword(email, newPassword);
    }

    /**
     * Store current user document path in database.
     */
    public boolean uploadDocument(String docName, String docPath) {
        String email = getCurrentUserEmail();
        if (email == null) return false;
        boolean success = profileDAO.uploadDocument(docName, docPath);
        if (success) {
            EventBus.getInstance().publish("PROFILE_DOCS_UPDATED", email);
        }
        return success;
    }

    /**
     * Store document path in database for specific email.
     */
    public boolean uploadDocument(String email, String docName, String docPath) {
        boolean success = profileDAO.uploadDocument(email, docName, docPath);
        if (success) {
            EventBus.getInstance().publish("PROFILE_DOCS_UPDATED", email);
        }
        return success;
    }

    /**
     * Retrieve current user documents.
     */
    public Map<String, String> getDocuments() {
        return profileDAO.getDocuments();
    }

    /**
     * Retrieve user documents by email.
     */
    public Map<String, String> getDocuments(String email) {
        return profileDAO.getDocuments(email);
    }

    /**
     * Remove current user document.
     */
    public boolean deleteDocument(String docName) {
        String email = getCurrentUserEmail();
        if (email == null) return false;
        boolean success = profileDAO.deleteDocument(docName);
        if (success) {
            EventBus.getInstance().publish("PROFILE_DOCS_UPDATED", email);
        }
        return success;
    }

    /**
     * Remove a document for specific email.
     */
    public boolean deleteDocument(String email, String docName) {
        boolean success = profileDAO.deleteDocument(email, docName);
        if (success) {
            EventBus.getInstance().publish("PROFILE_DOCS_UPDATED", email);
        }
        return success;
    }
}
