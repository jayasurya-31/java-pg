package shared;

import core.User;

/**
 * Singleton SessionManager to manage logged-in user and session state.
 * Stores only the current logged-in user for the duration of login.
 * Refactored to be thread-safe with improved logging.
 */
public class SessionManager {
    private static volatile SessionManager instance;
    private User currentUser;

    private SessionManager() {
        DebugLogger.info("SessionManager ? Singleton Instance Created.");
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Get the currently logged-in user.
     * @return current User object, null if not logged in
     */
    public User getCurrentUser() {
        System.out.println("[DEBUG] SessionManager.getCurrentUser() called, returning: " + currentUser);
        if (currentUser == null) {
            DebugLogger.warn("SessionManager ? Attempted to access currentUser but it is null.");
        }
        return currentUser;
    }

    /**
     * Set the current user directly.
     * @param user User object to store
     */
    public void setCurrentUser(User user) {
        System.out.println("[DEBUG] SessionManager.setCurrentUser() called with user: " + user);
        this.currentUser = user;
        if (user != null) {
            DebugLogger.info("SessionManager ? Current user set to: " + user.getEmail() + " (ID: " + user.getId() + ")");
        } else {
            DebugLogger.info("SessionManager ? Current user cleared (set to null)");
        }
    }

    /**
     * Log in a user and store session data.
     * @param user User object to log in
     */
    public void login(User user) {
        if (user != null) {
            this.currentUser = user;
            System.out.println("[DEBUG] AUTH_LOGIN_SUCCESS received. Initializing all modules for user: " + user.getId());
            EventBus.getInstance().publish("AUTH_LOGIN_SUCCESS", user);
            EventBus.getInstance().publish("INIT_MODULES_AFTER_LOGIN", user);
            DebugLogger.info("SessionManager ? User login successful: " + user.getEmail() + " (ID: " + user.getId() + ")");
        } else {
            DebugLogger.error("SessionManager ? Cannot log in null user.");
        }
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        if (currentUser != null) {
            DebugLogger.info("SessionManager ? User logout: " + currentUser.getEmail());
            currentUser = null;
        } else {
            DebugLogger.info("SessionManager ? Logout requested but no user was logged in.");
        }
    }

    /**
     * Alias for logout() to ensure backward compatibility.
     */
    public void logoutCurrentUser() {
        logout();
    }

    /**
     * Check if a user is currently logged in.
     * @return true if logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Helper to get current user ID from session.
     * @return userId if logged in, -1 otherwise.
     */
    public int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : -1;
    }
}
