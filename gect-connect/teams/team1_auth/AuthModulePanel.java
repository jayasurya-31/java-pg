package teams.team1_auth;

import shared.UIUtils;
import shared.AnimationUtils;
import shared.DebugLogger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main panel for Authentication module.
 * Manages Login, Register, and Forgot Password panels using modern components.
 */
public class AuthModulePanel extends JPanel {
    private final CardLayout cardLayout;
    private final UIUtils.FadePanel container;
    private final AuthService authService;

    public AuthModulePanel() {
        DebugLogger.info("AUTH ? Initializing module - constructor() - AuthModulePanel.java");
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);
        this.authService = new AuthService();

        cardLayout = new CardLayout();
        container = new UIUtils.FadePanel();
        container.setLayout(cardLayout);
        UIUtils.stylePanel(container);

        // Navigation listener
        java.util.function.Consumer<String> navigator = page -> {
            cardLayout.show(container, page);
            AnimationUtils.fadeIn(container, 400);
        };

        // Add sub-panels using the new redesigned classes
        container.add(new LoginPage(authService, navigator), "LOGIN");
        container.add(new RegisterPage(authService, navigator), "REGISTER");
        container.add(new ForgotPasswordPage(authService, navigator), "FORGOT_PASSWORD");

        add(container, BorderLayout.CENTER);

        // Apply fade-in effect when panel is first shown
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                AnimationUtils.fadeIn(container, 500);
            }
        });
    }
}
