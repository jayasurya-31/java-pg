package teams.team1_auth;

import shared.*;
import core.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Redesigned Register Page for GECT Connect with OTP verification.
 */
public class RegisterPage extends JPanel {
    private final AuthService authService;
    private final Consumer<String> navigator;
    private final Map<String, Integer> otpMap = new HashMap<>();

    private final JTextField nameField;
    private final JTextField emailField;
    private final JComboBox<String> roleDropdown;
    private final JTextField idField;
    private final JPasswordField passField;
    private final JPasswordField confirmPassField;
    private final JTextField otpField;
    private final JLabel otpDisplayLabel;
    private final JLabel errorLabel;
    private final JButton generateOtpBtn;
    private final JButton registerBtn;
    private final JPanel otpPanel;

    public RegisterPage(AuthService authService, Consumer<String> navigator) {
        DebugLogger.info("AUTH ? Initializing module - constructor() - RegisterPage.java");
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
        glassCard.setPreferredSize(new Dimension(500, 750));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 30, 8, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Title
        JLabel titleLabel = new JLabel("Create Account", JLabel.CENTER);
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_L));
        titleLabel.setForeground(UIUtils.SECONDARY_COLOR);
        glassCard.add(titleLabel, gbc);

        // Name
        gbc.gridy++;
        gbc.insets = new Insets(15, 30, 5, 30);
        nameField = UIUtils.createModernTextField("Full Name");
        nameField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        glassCard.add(nameField, gbc);

        // Email
        gbc.gridy++;
        gbc.insets = new Insets(10, 30, 5, 30);
        emailField = UIUtils.createModernTextField("College Email (@gectcr.ac.in)");
        emailField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        glassCard.add(emailField, gbc);

        // Role Selection
        gbc.gridy++;
        String[] roles = {"STUDENT", "STAFF"};
        roleDropdown = new JComboBox<>(roles);
        roleDropdown.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        roleDropdown.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        roleDropdown.setBackground(Color.WHITE);
        roleDropdown.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.BORDER_COLOR));
        glassCard.add(roleDropdown, gbc);

        // Roll No / Emp ID
        gbc.gridy++;
        idField = UIUtils.createModernTextField("Roll No");
        idField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        glassCard.add(idField, gbc);

        roleDropdown.addActionListener(e -> {
            String selected = (String) roleDropdown.getSelectedItem();
            idField.setText("STUDENT".equals(selected) ? "Roll No" : "Employee ID");
            idField.setForeground(Color.GRAY);
        });

        // Passwords
        gbc.gridy++;
        passField = UIUtils.createModernPasswordField("Create Password (min 6 chars)");
        passField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        glassCard.add(passField, gbc);

        gbc.gridy++;
        confirmPassField = UIUtils.createModernPasswordField("Confirm Password");
        confirmPassField.setPreferredSize(new Dimension(400, UIUtils.INPUT_HEIGHT));
        glassCard.add(confirmPassField, gbc);

        // OTP Section (Initially hidden)
        gbc.gridy++;
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

        // Error Message
        gbc.gridy++;
        errorLabel = new JLabel(" ", JLabel.LEFT);
        errorLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.CAPTION));
        errorLabel.setForeground(Color.RED);
        glassCard.add(errorLabel, gbc);

        // Generate OTP Button
        gbc.gridy++;
        generateOtpBtn = UIUtils.createRoundedButton("Generate OTP", UIUtils.PRIMARY_COLOR, Color.WHITE);
        generateOtpBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        glassCard.add(generateOtpBtn, gbc);

        // Register Button (Disabled)
        gbc.gridy++;
        registerBtn = UIUtils.createRoundedButton("Register", Color.decode("#28A745"), Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(400, UIUtils.BUTTON_HEIGHT));
        registerBtn.setEnabled(false);
        registerBtn.setVisible(false);
        glassCard.add(registerBtn, gbc);

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
        registerBtn.addActionListener(e -> handleRegister());

        // Validation for Register Button
        otpField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String enteredOtp = otpField.getText();
                String email = emailField.getText();
                Integer actualOtp = otpMap.get(email);
                registerBtn.setEnabled(actualOtp != null && enteredOtp.equals(String.valueOf(actualOtp)));
            }
        });
    }

    private void handleGenerateOtp() {
        DebugLogger.info("AUTH ? Form submitted: REGISTER_GENERATE_OTP - handleGenerateOtp() - RegisterPage.java");
        String name = nameField.getText();
        String email = emailField.getText();
        String id = idField.getText();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmPassField.getPassword());

        if (name.isEmpty() || email.isEmpty() || id.isEmpty() || pass.isEmpty()) {
            DebugLogger.warn("AUTH ? Validation failed: MISSING_FIELDS - handleGenerateOtp() - RegisterPage.java");
            errorLabel.setText("All fields are required.");
            AnimationUtils.shake(this);
            return;
        }

        if (!UIUtils.ValidationUtils.isValidEmail(email)) {
            DebugLogger.warn("AUTH ? Validation failed: INVALID_EMAIL - handleGenerateOtp() - RegisterPage.java");
            errorLabel.setText("Invalid email format. Use your college email like example@gectcr.ac.in");
            AnimationUtils.shake(this);
            return;
        }

        if (pass.length() < 6) {
            DebugLogger.warn("AUTH ? Validation failed: PASSWORD_TOO_SHORT - handleGenerateOtp() - RegisterPage.java");
            errorLabel.setText("Password must be at least 6 characters long.");
            AnimationUtils.shake(this);
            return;
        }

        if (!pass.equals(confirm)) {
            DebugLogger.warn("AUTH ? Validation failed: PASSWORD_MISMATCH - handleGenerateOtp() - RegisterPage.java");
            errorLabel.setText("Passwords do not match.");
            AnimationUtils.shake(this);
            return;
        }

        errorLabel.setText(" ");
        int otp = new Random().nextInt(900000) + 100000;
        otpMap.put(email, otp);
        DebugLogger.info("AUTH ? OTP Generated for " + email + " - handleGenerateOtp() - RegisterPage.java");
        otpDisplayLabel.setText("OTP: " + otp);
        otpPanel.setVisible(true);
        registerBtn.setVisible(true);
        generateOtpBtn.setText("Resend OTP");
        revalidate();
        repaint();
    }

    private void handleRegister() {
        DebugLogger.info("AUTH ? Form submitted: REGISTER_COMPLETE - handleRegister() - RegisterPage.java");
        String name = nameField.getText();
        String email = emailField.getText();
        String id = idField.getText();
        String role = (String) roleDropdown.getSelectedItem();
        String pass = new String(passField.getPassword());

        JDialog loading = UIUtils.showLoadingDialog(this, "Creating Account...");
        new Timer(1000, e -> {
            ((Timer)e.getSource()).stop();
            
            if (authService.isEmailExists(email)) {
                loading.dispose();
                DebugLogger.warn("AUTH ? Registration failed: EMAIL_EXISTS - handleRegister() - RegisterPage.java");
                errorLabel.setText("This email is already registered. Try logging in or use another email.");
                AnimationUtils.shake(this);
                return;
            }
            if (authService.isRollNoExists(id)) {
                loading.dispose();
                DebugLogger.warn("AUTH ? Registration failed: ID_EXISTS - handleRegister() - RegisterPage.java");
                errorLabel.setText("This Roll No / Employee ID already exists. Please use a unique ID.");
                AnimationUtils.shake(this);
                return;
            }

            boolean success = authService.register(email, name, role, id, pass);
            loading.dispose();
            if (success) {
                DebugLogger.info("AUTH ? Registration successful for " + email + " - handleRegister() - RegisterPage.java");
                UIUtils.showToast("Registration completed successfully. You can now log in.", this);
                navigator.accept("LOGIN");
            } else {
                DebugLogger.error("AUTH ? Registration failed: DAO_ERROR - handleRegister() - RegisterPage.java");
                errorLabel.setText("Registration failed. Please check the input and try again.");
                AnimationUtils.shake(this);
            }
        }).start();
        loading.setVisible(true);
    }
}
