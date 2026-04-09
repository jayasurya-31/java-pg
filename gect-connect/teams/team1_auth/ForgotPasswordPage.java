package teams.team1_auth;

import shared.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Redesigned Forgot Password Page for GECT Connect with OTP verification.
 */
public class ForgotPasswordPage extends JPanel {
    private final AuthService authService;
    private final Consumer<String> navigator;
    private final Map<String, Integer> otpMap = new HashMap<>();

    private final JTextField emailField;
    private final JPasswordField newPassField;
    private final JPasswordField confirmPassField;
    private final JTextField otpField;
    private final JLabel otpDisplayLabel;
    private final JLabel errorLabel;
    private final JButton generateOtpBtn;
    private final JButton updatePassBtn;
    private final JPanel otpPanel;
    private final JPanel passResetPanel;

    public ForgotPasswordPage(AuthService authService, Consumer<String> navigator) {
        this.authService = authService;
        this.navigator = navigator;

        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        
        // Background with gradient effect
        JPanel bgPanel = new WatermarkPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.LIGHT_PRIMARY, getWidth(), getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        add(bgPanel, BorderLayout.CENTER);

        // Glassmorphism Card
        JPanel glassCard = UIUtils.createGlassPanel(24);
        glassCard.setLayout(new GridBagLayout());
        glassCard.setPreferredSize(new Dimension(450, 650));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Title
        JLabel titleLabel = new JLabel("Reset Password", JLabel.CENTER);
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_L));
        titleLabel.setForeground(UIUtils.SECONDARY_COLOR);
        glassCard.add(titleLabel, gbc);

        // Email
        gbc.gridy++;
        gbc.insets = new Insets(25, 30, 5, 30);
        emailField = UIUtils.createModernTextField("College Email (@gectcr.ac.in)");
        emailField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        glassCard.add(emailField, gbc);

        // OTP Section (Initially hidden)
        gbc.gridy++;
        gbc.insets = new Insets(15, 30, 5, 30);
        otpPanel = new JPanel(new BorderLayout(UIUtils.COMPONENT_GAP, 0));
        otpPanel.setOpaque(false);
        otpPanel.setVisible(false);

        otpField = UIUtils.createModernTextField("6-digit OTP");
        otpField.setPreferredSize(new Dimension(200, UIUtils.INPUT_HEIGHT));
        otpPanel.add(otpField, BorderLayout.CENTER);

        otpDisplayLabel = new JLabel("OTP: -", JLabel.CENTER);
        otpDisplayLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        otpDisplayLabel.setForeground(UIUtils.PRIMARY_COLOR);
        otpPanel.add(otpDisplayLabel, BorderLayout.EAST);
        glassCard.add(otpPanel, gbc);

        // Password Reset Section (Initially hidden)
        gbc.gridy++;
        gbc.insets = new Insets(15, 30, 5, 30);
        passResetPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        passResetPanel.setOpaque(false);
        passResetPanel.setVisible(false);

        newPassField = UIUtils.createModernPasswordField("New Password (min 6 chars)");
        newPassField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        passResetPanel.add(newPassField);

        confirmPassField = UIUtils.createModernPasswordField("Confirm New Password");
        confirmPassField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        passResetPanel.add(confirmPassField);
        glassCard.add(passResetPanel, gbc);

        // Error Message
        gbc.gridy++;
        gbc.insets = new Insets(5, 30, 5, 30);
        errorLabel = new JLabel(" ", JLabel.LEFT);
        errorLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.CAPTION));
        errorLabel.setForeground(Color.RED);
        glassCard.add(errorLabel, gbc);

        // Generate OTP Button
        gbc.gridy++;
        gbc.insets = new Insets(15, 30, 10, 30);
        generateOtpBtn = UIUtils.createRoundedButton("Send OTP", UIUtils.PRIMARY_COLOR, Color.WHITE);
        generateOtpBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        glassCard.add(generateOtpBtn, gbc);

        // Update Password Button (Initially hidden/disabled)
        gbc.gridy++;
        updatePassBtn = UIUtils.createRoundedButton("Update Password", Color.decode("#FFC107"), Color.WHITE);
        updatePassBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        updatePassBtn.setEnabled(false);
        updatePassBtn.setVisible(false);
        glassCard.add(updatePassBtn, gbc);

        // Back to Login
        gbc.gridy++;
        JButton backBtn = new JButton("Back to Login");
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setForeground(UIUtils.TEXT_SECONDARY);
        backBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> navigator.accept("LOGIN"));
        glassCard.add(backBtn, gbc);

        bgPanel.add(glassCard);

        // Logic
        generateOtpBtn.addActionListener(e -> handleGenerateOtp());
        updatePassBtn.addActionListener(e -> handleUpdatePassword());

        // OTP verification real-time
        otpField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String enteredOtp = otpField.getText();
                String email = emailField.getText();
                Integer actualOtp = otpMap.get(email);
                boolean verified = actualOtp != null && enteredOtp.equals(String.valueOf(actualOtp));
                passResetPanel.setVisible(verified);
                updatePassBtn.setVisible(verified);
                updatePassBtn.setEnabled(verified);
                if (verified) {
                    errorLabel.setText("OTP Verified Successfully");
                    errorLabel.setForeground(Color.decode("#28A745"));
                } else {
                    errorLabel.setForeground(Color.RED);
                }
                revalidate();
                repaint();
            }
        });
    }

    private void handleGenerateOtp() {
        DebugLogger.info("AUTH ? Form submitted: FORGOT_PASSWORD_GENERATE_OTP - handleGenerateOtp() - ForgotPasswordPage.java");
        String email = emailField.getText();

        if (email.isEmpty() || !UIUtils.ValidationUtils.isValidEmail(email)) {
            DebugLogger.warn("AUTH ? Validation failed: INVALID_EMAIL - handleGenerateOtp() - ForgotPasswordPage.java");
            errorLabel.setText("Invalid email format. Please enter a valid college email.");
            AnimationUtils.shake(this);
            return;
        }

        // Mocking email check using DAO
        if (!authService.isEmailExists(email)) {
            DebugLogger.warn("AUTH ? Validation failed: ACCOUNT_NOT_FOUND - handleGenerateOtp() - ForgotPasswordPage.java");
            errorLabel.setText("No account found with this email.");
            AnimationUtils.shake(this);
            return;
        }

        errorLabel.setText(" ");
        int otp = new Random().nextInt(900000) + 100000;
        otpMap.put(email, otp);
        DebugLogger.info("AUTH ? OTP Generated for " + email + " - handleGenerateOtp() - ForgotPasswordPage.java");
        otpDisplayLabel.setText("OTP: " + otp);
        otpPanel.setVisible(true);
        generateOtpBtn.setText("Resend OTP");
        revalidate();
        repaint();
        UIUtils.showToast("Password reset instructions have been sent to your email.", this);
    }

    private void handleUpdatePassword() {
        DebugLogger.info("AUTH ? Form submitted: FORGOT_PASSWORD_UPDATE - handleUpdatePassword() - ForgotPasswordPage.java");
        String email = emailField.getText();
        String pass = new String(newPassField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (pass.length() < 6) {
            DebugLogger.warn("AUTH ? Validation failed: PASSWORD_TOO_SHORT - handleUpdatePassword() - ForgotPasswordPage.java");
            errorLabel.setText("Invalid input detected. Ensure all fields are correctly filled and meet requirements.");
            AnimationUtils.shake(this);
            return;
        }

        if (!pass.equals(confirm)) {
            DebugLogger.warn("AUTH ? Validation failed: PASSWORD_MISMATCH - handleUpdatePassword() - ForgotPasswordPage.java");
            errorLabel.setText("Invalid input detected. Ensure all fields are correctly filled and meet requirements.");
            AnimationUtils.shake(this);
            return;
        }

        JDialog loading = UIUtils.showLoadingDialog(this, "Updating Password...");
        new Timer(1000, e -> {
            boolean success = updatePasswordBackend(email, pass);
            loading.dispose();
            if (success) {
                DebugLogger.info("AUTH ? Password updated successfully for " + email + " - handleUpdatePassword() - ForgotPasswordPage.java");
                UIUtils.showToast("Password updated successfully! You can now log in.", this);
                navigator.accept("LOGIN");
            } else {
                DebugLogger.error("AUTH ? Connection FAILED: PASSWORD_UPDATE_FAILED - handleUpdatePassword() - ForgotPasswordPage.java");
                errorLabel.setText("Unable to send password reset instructions. Try again later.");
                AnimationUtils.shake(this);
            }
            ((Timer)e.getSource()).stop();
        }).start();
        loading.setVisible(true);
    }

    private boolean updatePasswordBackend(String email, String newPassword) {
        // Implementation for AuthDAO.updatePassword
        return new AuthDAO().updatePassword(email, newPassword);
    }
}
