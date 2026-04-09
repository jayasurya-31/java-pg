package teams.team1_auth;

import core.User;
import shared.SessionManager;
import shared.EventBus;
import shared.PasswordUtils;
import shared.UIUtils;
import shared.DebugLogger;

/**
 * Service for Authentication module logic.
 * Refactored to use SessionManager for current user context.
 */
public class AuthService {
    private final AuthDAO authDAO;

    public AuthService() {
        DebugLogger.info("AUTH ? Initializing service - constructor() - AuthService.java");
        this.authDAO = new AuthDAO();
    }

    /**
     * Authenticate user and create a session.
     * @param email User email
     * @param password User password
     * @return User object if successful, null otherwise
     */
    public User login(String email, String password) {
        DebugLogger.info("AUTH ? Login attempt: " + email + " - login() - AuthService.java");
        email = UIUtils.ValidationUtils.trim(email);
        
        // Domain validation: @gectcr.ac.in check
        if (!UIUtils.ValidationUtils.isValidEmail(email)) {
            DebugLogger.warn("AUTH ? Login FAILED: Invalid Email Domain - " + email + " - login() - AuthService.java");
            return null;
        }

        DebugLogger.info("AUTH ? Checking credentials via DAO... - login() - AuthService.java");
        User user = authDAO.authenticate(email, password);
        if (user != null) {
            // Success: Create session and publish event
            DebugLogger.info("AUTH ? Login SUCCESS: User found - " + user.getFullName() + " - login() - AuthService.java");
            SessionManager.getInstance().login(user);
            EventBus.getInstance().publish("AUTH_LOGIN_SUCCESS", user);
        } else {
            DebugLogger.warn("AUTH ? Login FAILED: Invalid credentials for - " + email + " - login() - AuthService.java");
        }
        return user;
    }

    /**
     * Register a new user with validation.
     */
    public boolean register(String email, String fullName, String role, String rollNoEmpId, String department, String password) {
        DebugLogger.info("AUTH ? Registration attempt: " + email + " - register() - AuthService.java");
        email = UIUtils.ValidationUtils.trim(email);
        fullName = UIUtils.ValidationUtils.trim(fullName);
        rollNoEmpId = UIUtils.ValidationUtils.trim(rollNoEmpId);
        department = UIUtils.ValidationUtils.trim(department);

        // All fields required
        if (email.isEmpty() || fullName.isEmpty() || role == null || rollNoEmpId.isEmpty() || department.isEmpty() || password.isEmpty()) {
            DebugLogger.warn("AUTH ? Registration FAILED: Missing fields - register() - AuthService.java");
            return false;
        }

        // Email validation
        if (!UIUtils.ValidationUtils.isValidEmail(email)) {
            DebugLogger.warn("AUTH ? Registration FAILED: Invalid email - " + email + " - register() - AuthService.java");
            return false;
        }

        // Password validation
        if (password.length() < 6) {
            DebugLogger.warn("AUTH ? Registration FAILED: Password too short - register() - AuthService.java");
            return false;
        }

        // Name validation (min 3 chars)
        if (fullName.length() < 3) {
            DebugLogger.warn("AUTH ? Registration FAILED: Name too short - register() - AuthService.java");
            return false;
        }

        // Check if user already exists
        DebugLogger.info("AUTH ? Checking if user exists... - register() - AuthService.java");
        if (authDAO.isEmailExists(email)) {
            DebugLogger.warn("AUTH ? Registration FAILED: Email already exists - " + email + " - register() - AuthService.java");
            return false;
        }

        DebugLogger.info("AUTH ? Calling DAO register... - register() - AuthService.java");
        boolean success = authDAO.register(email, fullName, role, rollNoEmpId, department, password);
        if (success) {
            DebugLogger.info("AUTH ? Registration SUCCESS: " + email + " - register() - AuthService.java");
            EventBus.getInstance().publish("AUTH_REGISTER_SUCCESS", email);
        } else {
            DebugLogger.error("AUTH ? Registration FAILED in DAO: " + email + " - register() - AuthService.java");
        }
        return success;
    }

    public boolean isEmailExists(String email) {
        DebugLogger.info("AUTH ? Checking email existence: " + email + " - isEmailExists() - AuthService.java");
        return authDAO.isEmailExists(email);
    }

    public boolean isRollNoExists(String rollNo) {
        DebugLogger.info("AUTH ? Checking roll no existence: " + rollNo + " - isRollNoExists() - AuthService.java");
        return authDAO.isRollNoExists(rollNo);
    }

    /**
     * Logout current user and update status.
     */
    public void logout() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            logout(user);
        }
    }

    /**
     * Logout specified user and update status.
     */
    public void logout(User user) {
        if (user != null) {
            authDAO.updateOnlineStatus(user.getUserId(), false);
            if (user.equals(SessionManager.getInstance().getCurrentUser())) {
                SessionManager.getInstance().logout();
            }
        }
    }
}