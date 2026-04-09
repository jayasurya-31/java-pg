package teams.team1_auth;

import shared.*;
import core.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

/**
 * Redesigned Login Page for GECT Connect.
 */
public class LoginPage extends JPanel {
    private final AuthService authService;
    private final Consumer<String> navigator;
    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JLabel errorLabel;
    private final JButton loginBtn;
    private boolean isPasswordVisible = false;

    public LoginPage(AuthService authService, Consumer<String> navigator) {
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
        glassCard.setPreferredSize(new Dimension(450, 600));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Logo
        JLabel logoLabel = new JLabel(shared.Icons.get("main_logo"), JLabel.CENTER);
        glassCard.add(logoLabel, gbc);

        // Title
        gbc.gridy++;
        JLabel titleLabel = new JLabel("Login to GECT Connect", JLabel.CENTER);
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_L));
        titleLabel.setForeground(UIUtils.SECONDARY_COLOR);
        glassCard.add(titleLabel, gbc);

        // Subtitle
        gbc.gridy++;
        JLabel subtitleLabel = new JLabel("Enter your credentials to continue", JLabel.CENTER);
        subtitleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        subtitleLabel.setForeground(UIUtils.TEXT_SECONDARY);
        glassCard.add(subtitleLabel, gbc);

        // Email Field
        gbc.gridy++;
        gbc.insets = new Insets(25, 30, 5, 30);
        emailField = UIUtils.createModernTextField("example@gectcr.ac.in");
        emailField.setPreferredSize(new Dimension(350, UIUtils.INPUT_HEIGHT));
        glassCard.add(emailField, gbc);

        // Password Field with Toggle
        gbc.gridy++;
        gbc.insets = new Insets(15, 30, 5, 30);
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setOpaque(false);
        passwordField = UIUtils.createModernPasswordField("Enter password");
        passwordField.setPreferredSize(new Dimension(300, UIUtils.INPUT_HEIGHT));
        passPanel.add(passwordField, BorderLayout.CENTER);

        JButton toggleBtn = new JButton("Show");
        toggleBtn.setPreferredSize(new Dimension(70, UIUtils.INPUT_HEIGHT));
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        toggleBtn.addActionListener(e -> {
            isPasswordVisible = !isPasswordVisible;
            passwordField.setEchoChar(isPasswordVisible ? (char) 0 : '*');
            toggleBtn.setText(isPasswordVisible ? "Hide" : "Show");
        });
        passPanel.add(toggleBtn, BorderLayout.EAST);
        glassCard.add(passPanel, gbc);

        // Error Message Label
        gbc.gridy++;
        gbc.insets = new Insets(0, 30, 5, 30);
        errorLabel = new JLabel(" ", JLabel.LEFT);
        errorLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.CAPTION));
        errorLabel.setForeground(Color.RED);
        glassCard.add(errorLabel, gbc);

        // Forgot Password Link
        gbc.gridy++;
        gbc.insets = new Insets(0, 30, 20, 30);
        JButton forgotBtn = new JButton("Forgot Password?");
        forgotBtn.setHorizontalAlignment(SwingConstants.RIGHT);
        forgotBtn.setBorderPainted(false);
        forgotBtn.setContentAreaFilled(false);
        forgotBtn.setForeground(UIUtils.TEXT_SECONDARY);
        forgotBtn.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        forgotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotBtn.addActionListener(e -> navigator.accept("FORGOT_PASSWORD"));
        glassCard.add(forgotBtn, gbc);

        // Login Button
        gbc.gridy++;
        gbc.insets = new Insets(10, 30, 10, 30);
        loginBtn = UIUtils.createRoundedButton("Login", UIUtils.PRIMARY_COLOR, Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(350, UIUtils.BUTTON_HEIGHT));
        loginBtn.setEnabled(false);
        glassCard.add(loginBtn, gbc);

        // Register Link
        gbc.gridy++;
        JButton registerLink = new JButton("Don't have an account? Register");
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setFocusPainted(false);
        registerLink.setForeground(UIUtils.PRIMARY_COLOR);
        registerLink.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_SMALL));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> navigator.accept("REGISTER"));
        glassCard.add(registerLink, gbc);

        bgPanel.add(glassCard);

        // Validation Logic
        KeyAdapter validationKeyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateFields();
                if (e.getKeyCode() == KeyEvent.VK_ENTER && loginBtn.isEnabled()) {
                    performLogin();
                }
            }
        };
        emailField.addKeyListener(validationKeyAdapter);
        passwordField.addKeyListener(validationKeyAdapter);

        loginBtn.addActionListener(e -> {
            DebugLogger.info("AUTH ? CLICK_LOGIN on loginBtn - constructor() - LoginPage.java");
            performLogin();
        });
    }

    private void validateFields() {
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());
        
        boolean isEmailValid = UIUtils.ValidationUtils.isValidEmail(email);
        boolean isPassValid = pass.length() >= 6;
        
        if (email.isEmpty() || email.equals("example@gectcr.ac.in")) {
            errorLabel.setText(" ");
        } else if (!isEmailValid) {
            errorLabel.setText("Invalid email format. Use your college email like example@gectcr.ac.in");
        } else {
            errorLabel.setText(" ");
        }
        
        loginBtn.setEnabled(isEmailValid && isPassValid);
    }

    private void performLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        DebugLogger.info("AUTH ? Login button clicked - performLogin() - LoginPage.java");
        loginBtn.setEnabled(false);
        errorLabel.setText(" ");

        JDialog loading = UIUtils.showLoadingDialog(this, "Authenticating...");
        
        DebugLogger.info("AUTH ? Starting background authentication - performLogin() - LoginPage.java");
        SwingWorker<User, Void> worker = new SwingWorker<User, Void>() {
            @Override
            protected User doInBackground() throws Exception {
                // Step 1: Check if email exists
                if (!authService.isEmailExists(email)) {
                    return null; // Signals account not found
                }
                // Step 2: Attempt login
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loading.dispose();
                loginBtn.setEnabled(true);
                DebugLogger.info("AUTH ? Authentication completed - done() - LoginPage.java");

                try {
                    User user = get();
                    if (user != null) {
                        DebugLogger.info("AUTH ? Login successful, navigating to LANDING - done() - LoginPage.java");
                        errorLabel.setText("Login successful!");
                        errorLabel.setForeground(Color.decode("#28A745"));
                        UIUtils.showToast("Welcome back, " + user.getFullName(), LoginPage.this);
                        navigator.accept("LANDING");
                    } else {
                        // Check why it failed (account not found vs invalid password)
                        new Thread(() -> {
                            boolean exists = authService.isEmailExists(email);
                            SwingUtilities.invokeLater(() -> {
                                if (!exists) {
                                    DebugLogger.warn("AUTH ? Login failed: Account not found - done() - LoginPage.java");
                                    errorLabel.setText("Account not found. Please register.");
                                } else {
                                    DebugLogger.warn("AUTH ? Login failed: Invalid password - done() - LoginPage.java");
                                    errorLabel.setText("Invalid password. Please try again.");
                                }
                                errorLabel.setForeground(Color.RED);
                                AnimationUtils.shake(LoginPage.this);
                            });
                        }).start();
                    }
                } catch (Exception ex) {
                    DebugLogger.error("AUTH ? Login error: " + ex.getMessage() + " - done() - LoginPage.java");
                    errorLabel.setText("An error occurred during login.");
                    errorLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
        loading.setVisible(true);
    }
}
