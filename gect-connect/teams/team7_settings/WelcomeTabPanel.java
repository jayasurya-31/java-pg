package teams.team7_settings;

import core.User;
import shared.SessionManager;
import shared.UIUtils;
import shared.DebugLogger;
import javax.swing.*;
import java.awt.*;

/**
 * WelcomeTabPanel class following the JSON specification.
 * Handles user profile display and first login instructions.
 */
public class WelcomeTabPanel extends JPanel {
    private final JLabel profileNameLabel;
    private final JLabel profileEmailLabel;
    private final JLabel profileImageLabel;
    private final JLabel welcomeMsgLabel;

    public WelcomeTabPanel() {
        setLayout(new GridBagLayout());
        setBackground(UIUtils.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel header = new JLabel("Welcome to GECT Connect");
        header.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_XL));
        header.setForeground(UIUtils.TEXT_PRIMARY);
        add(header, gbc);

        gbc.gridy++;
        JLabel subHeader = new JLabel("Get Started");
        subHeader.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.HEADING_M));
        subHeader.setForeground(UIUtils.TEXT_SECONDARY);
        add(subHeader, gbc);

        gbc.gridy++;
        
        // Profile Card
        JPanel card = UIUtils.createNeumorphicPanel(16);
        card.setLayout(new BorderLayout(20, 0));
        card.setPreferredSize(new Dimension(500, 120));
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        UIUtils.applyHoverLift(card);

        profileImageLabel = new JLabel(shared.Icons.get("user_avatar"));
        card.add(profileImageLabel, BorderLayout.WEST);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setOpaque(false);
        
        welcomeMsgLabel = new JLabel("Welcome back!");
        welcomeMsgLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, UIUtils.BODY_SMALL));
        
        profileNameLabel = new JLabel();
        profileNameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.BODY_LARGE));
        
        profileEmailLabel = new JLabel();
        profileEmailLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_NORMAL));
        profileEmailLabel.setForeground(Color.GRAY);

        info.add(welcomeMsgLabel);
        info.add(profileNameLabel);
        info.add(profileEmailLabel);
        card.add(info, BorderLayout.CENTER);
        
        add(card, gbc);

        // Fetch User Profile
        fetchUserProfile();
    }

    /**
     * Implementation of fetchUserProfile action from JSON spec.
     */
    private void fetchUserProfile() {
        DebugLogger.info("WELCOME ? Fetching user profile - fetchUserProfile() - WelcomeTabPanel.java");
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            profileNameLabel.setText(currentUser.getName());
            profileEmailLabel.setText(currentUser.getEmail());
            Icon profileImage = currentUser.getProfileImage();
            if (profileImage != null) {
                profileImageLabel.setIcon(profileImage);
                profileImageLabel.setText("");
            }
        } else {
            profileNameLabel.setText("Guest User");
            profileEmailLabel.setText("No email available");
        }
    }
}
