package shared;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility for password hashing and verification using SHA-256.
 */
public class PasswordUtils {

    /**
     * Hash a password using SHA-256 and return Base64 encoded string.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback to plain text if algorithm not found (not recommended)
        }
    }

    /**
     * Verify a password against a hash.
     */
    public static boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}
