package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;

/**
 * GUI screen for editing user status message.
 */
public class EditStatusScreen extends JPanel {
    private final ProfileModulePanel parent;
    private final ProfileService profileService;
    private final JTextArea statusArea;

    public EditStatusScreen(ProfileModulePanel parent, ProfileService profileService) {
        this.parent = parent;
        this.profileService = profileService;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Update Status", JLabel.CENTER);
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        add(title, gbc);

        gbc.gridy++;
        statusArea = new JTextArea(4, 30);
        statusArea.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));
        statusArea.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR));
        add(new JScrollPane(statusArea), gbc);

        gbc.gridy++;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actions.setOpaque(false);

        JButton saveBtn = UIUtils.createRoundedButton("Save", UIUtils.PRIMARY_COLOR, Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(150, 45));
        saveBtn.addActionListener(e -> saveStatus());

        JButton cancelBtn = UIUtils.createRoundedButton("Cancel", Color.GRAY, Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(150, 45));
        cancelBtn.addActionListener(e -> parent.showScreen("VIEW_PROFILE"));

        actions.add(saveBtn);
        actions.add(cancelBtn);
        add(actions, gbc);
    }

    public void loadCurrentStatus() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;
        
        new Thread(() -> {
            User user = profileService.getProfile();
            if (user != null) {
                SwingUtilities.invokeLater(() -> statusArea.setText(user.getStatus()));
            }
        }).start();
    }

    private void saveStatus() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        // Requirement: Update ONLY status without overwriting other fields
        if (profileService.updateStatus(statusArea.getText())) {
            UIUtils.showToast("Status Updated!", this);
            EventBus.getInstance().publish("PROFILE_UPDATED", self.getEmail());
            parent.showScreen("VIEW_PROFILE");
        }
    }
}
