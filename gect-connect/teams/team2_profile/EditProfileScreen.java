package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;

/**
 * GUI screen for editing user profile information.
 */
public class EditProfileScreen extends JPanel {
    private final ProfileModulePanel parent;
    private final ProfileService profileService;
    private final JTextField nameField;
    private final JTextField deptField;
    private final JTextField rollField;
    private final JTextField mobileField;

    public EditProfileScreen(ProfileModulePanel parent, ProfileService profileService) {
        this.parent = parent;
        this.profileService = profileService;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Edit Profile Information", JLabel.CENTER);
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        add(title, gbc);

        gbc.gridy++;
        nameField = UIUtils.createModernTextField("Full Name");
        add(nameField, gbc);

        gbc.gridy++;
        deptField = UIUtils.createModernTextField("Department");
        add(deptField, gbc);

        gbc.gridy++;
        rollField = UIUtils.createModernTextField("Roll No / Emp ID");
        add(rollField, gbc);

        gbc.gridy++;
        mobileField = UIUtils.createModernTextField("Mobile");
        add(mobileField, gbc);

        gbc.gridy++;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actions.setOpaque(false);

        JButton saveBtn = UIUtils.createRoundedButton("Save", UIUtils.PRIMARY_COLOR, Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(150, 45));
        saveBtn.addActionListener(e -> saveChanges());

        JButton cancelBtn = UIUtils.createRoundedButton("Cancel", Color.GRAY, Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(150, 45));
        cancelBtn.addActionListener(e -> parent.showScreen("VIEW_PROFILE"));

        actions.add(saveBtn);
        actions.add(cancelBtn);
        add(actions, gbc);
    }

    public void loadProfileData() {
        new Thread(() -> {
            User user = profileService.getProfile();
            if (user != null) {
                SwingUtilities.invokeLater(() -> {
                    nameField.setText(user.getFullName());
                    deptField.setText(user.getDepartment());
                    rollField.setText(user.getRollNoEmpId());
                    mobileField.setText(user.getMobile());
                });
            }
        }).start();
    }

    private void saveChanges() {
        // Requirement: Only update fields that were changed or are non-empty
        String name = nameField.getText().trim();
        String dept = deptField.getText().trim();
        String roll = rollField.getText().trim();
        String mobile = mobileField.getText().trim();

        if (profileService.updateProfile(
                name.isEmpty() ? null : name, 
                null, 
                mobile.isEmpty() ? null : mobile, 
                null, 
                dept.isEmpty() ? null : dept, 
                roll.isEmpty() ? null : roll)) {
            
            UIUtils.showToast("Profile Updated!", this);
            parent.showScreen("VIEW_PROFILE");
        }
    }
}
