package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;

/**
 * GUI screen for securely changing user password.
 */
public class ChangePasswordScreen extends JPanel {
    private final ProfileService profileService;
    private final JPasswordField oldPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;

    public ChangePasswordScreen(ProfileService profileService) {
        this.profileService = profileService;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Security Settings", JLabel.CENTER);
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        add(title, gbc);

        gbc.gridy++;
        add(new JLabel("Old Password:"), gbc);
        gbc.gridy++;
        oldPasswordField = new JPasswordField(20);
        oldPasswordField.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));
        add(oldPasswordField, gbc);

        gbc.gridy++;
        add(new JLabel("New Password:"), gbc);
        gbc.gridy++;
        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));
        add(newPasswordField, gbc);

        gbc.gridy++;
        add(new JLabel("Confirm New Password:"), gbc);
        gbc.gridy++;
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));
        add(confirmPasswordField, gbc);

        gbc.gridy++;
        JButton changeBtn = UIUtils.createRoundedButton("Update Password", UIUtils.PRIMARY_COLOR, Color.WHITE);
        changeBtn.setPreferredSize(new Dimension(200, 45));
        changeBtn.addActionListener(e -> changePassword());
        add(changeBtn, gbc);
    }

    private void changePassword() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String oldPass = new String(oldPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            UIUtils.showToast("Fields cannot be empty!", this);
            return;
        }

        if (!newPass.equals(confirmPass)) {
            UIUtils.showToast("Passwords do not match!", this);
            return;
        }

        if (profileService.changePassword(currentUser.getEmail(), newPass)) {
            UIUtils.showToast("Password Updated!", this);
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            UIUtils.showToast("Update Failed!", this);
        }
    }
}
