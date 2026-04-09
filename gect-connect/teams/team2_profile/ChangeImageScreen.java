package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * GUI screen for changing profile picture.
 */
public class ChangeImageScreen extends JPanel {
    private final ProfileModulePanel parent;
    private final ProfileService profileService;
    private final JLabel imagePreview;
    private String selectedPath = null;

    public ChangeImageScreen(ProfileModulePanel parent, ProfileService profileService) {
        this.parent = parent;
        this.profileService = profileService;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Update Profile Picture", JLabel.CENTER);
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        add(title, gbc);

        gbc.gridy++;
        imagePreview = new JLabel();
        imagePreview.setPreferredSize(new Dimension(200, 200));
        imagePreview.setHorizontalAlignment(JLabel.CENTER);
        imagePreview.setOpaque(true);
        imagePreview.setBackground(UIUtils.LIGHT_BLUE);
        imagePreview.setIcon(Icons.get("user_avatar", 150));
        add(imagePreview, gbc);

        gbc.gridy++;
        JButton uploadBtn = UIUtils.createRoundedButton("Upload New Image", Color.WHITE, UIUtils.TEXT_PRIMARY);
        uploadBtn.setPreferredSize(new Dimension(200, 45));
        uploadBtn.addActionListener(e -> selectImage());
        add(uploadBtn, gbc);

        gbc.gridy++;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        actions.setOpaque(false);

        JButton saveBtn = UIUtils.createRoundedButton("Save", UIUtils.PRIMARY_COLOR, Color.WHITE);
        saveBtn.setPreferredSize(new Dimension(150, 45));
        saveBtn.addActionListener(e -> saveImage());

        JButton cancelBtn = UIUtils.createRoundedButton("Cancel", Color.GRAY, Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(150, 45));
        cancelBtn.addActionListener(e -> parent.showScreen("VIEW_PROFILE"));

        actions.add(saveBtn);
        actions.add(cancelBtn);
        add(actions, gbc);
    }

    private void selectImage() {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg", "gif"));
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPath = jfc.getSelectedFile().getAbsolutePath();
            updatePreview(selectedPath);
        }
    }

    private void updatePreview(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imagePreview.setIcon(Icons.get("user_avatar", 150));
        }
    }

    private void saveImage() {
        if (selectedPath == null) {
            UIUtils.showToast("Please select an image first!", this);
            return;
        }
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;

        // Requirement: Update ONLY profile picture without overwriting other fields
        if (profileService.updateProfilePicture(selectedPath)) {
            UIUtils.showToast("Photo Updated!", this);
            EventBus.getInstance().publish("PROFILE_UPDATED", self.getEmail());
            parent.showScreen("VIEW_PROFILE");
        }
    }

    public void loadCurrentImage() {
        User self = SessionManager.getInstance().getCurrentUser();
        if (self == null) return;
        
        new Thread(() -> {
            User user = profileService.getProfile();
            if (user != null && user.getProfilePic() != null) {
                SwingUtilities.invokeLater(() -> updatePreview(user.getProfilePic()));
            }
        }).start();
    }
}
