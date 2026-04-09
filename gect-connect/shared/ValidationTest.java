package shared;

/**
 * Simple test utility to verify ValidationUtils logic.
 */
public class ValidationTest {
    public static void main(String[] args) {
        System.out.println("Running ValidationUtils Tests...");

        // Email Tests
        assert UIUtils.ValidationUtils.isValidEmail("user@gectcr.ac.in") : "Valid email failed";
        assert !UIUtils.ValidationUtils.isValidEmail("user@gmail.com") : "Invalid email domain passed";
        assert !UIUtils.ValidationUtils.isValidEmail("user@gectcr.ac.in.com") : "Invalid email suffix passed";

        // Password Tests
        assert UIUtils.ValidationUtils.isValidPassword("Pass123") : "Valid password failed";
        assert !UIUtils.ValidationUtils.isValidPassword("abc") : "Short password passed";
        assert !UIUtils.ValidationUtils.isValidPassword("password123") : "No uppercase password passed";
        assert !UIUtils.ValidationUtils.isValidPassword("PASSWORD123") : "No lowercase password passed";

        // Trimming Tests
        assert UIUtils.ValidationUtils.trim("  hello  ").equals("hello") : "Trimming failed";
        assert UIUtils.ValidationUtils.trim(null).equals("") : "Null trimming failed";

        System.out.println("All tests passed!");
    }
}
