package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * GUI screen for displaying user profile information and navigation.
 */
public class ViewProfileScreen extends JPanel {
    private final ProfileModulePanel parent;
    private final ProfileService profileService;
    private final JLabel profilePicLabel;
    private final JLabel nameLabel;
    private final JLabel emailLabel;
    private final JLabel departmentLabel;
    private final JLabel statusLabel;
    private final JLabel rollNoLabel;

    public ViewProfileScreen(ProfileModulePanel parent, ProfileService profileService) {
        this.parent = parent;
        this.profileService = profileService;
        setLayout(new GridBagLayout());
        UIUtils.stylePanel(this);

        JPanel card = UIUtils.createNeumorphicPanel(24);
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(550, 750));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        UIUtils.applyHoverLift(card);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;

        profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(180, 180));
        profilePicLabel.setHorizontalAlignment(JLabel.CENTER);
        profilePicLabel.setOpaque(true);
        profilePicLabel.setBackground(UIUtils.LIGHT_BLUE);
        profilePicLabel.setIcon(Icons.get("user_avatar", 120));
        card.add(profilePicLabel, gbc);

        gbc.gridy++;
        nameLabel = new JLabel("—");
        nameLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_L));
        card.add(nameLabel, gbc);

        gbc.gridy++;
        emailLabel = new JLabel("—");
        emailLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_NORMAL));
        emailLabel.setForeground(Color.GRAY);
        card.add(emailLabel, gbc);

        gbc.gridy++;
        statusLabel = new JLabel("—");
        statusLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, UIUtils.BODY_NORMAL));
        statusLabel.setForeground(Color.GRAY);
        card.add(statusLabel, gbc);

        gbc.gridy++;
        departmentLabel = new JLabel("—");
        departmentLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_NORMAL));
        departmentLabel.setForeground(UIUtils.PRIMARY_COLOR);
        card.add(departmentLabel, gbc);

        gbc.gridy++;
        rollNoLabel = new JLabel("—");
        rollNoLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, UIUtils.BODY_SMALL));
        card.add(rollNoLabel, gbc);

        // Buttons Grid
        gbc.gridy++;
        gbc.insets = new Insets(20, 10, 5, 10);
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        btnGrid.setOpaque(false);

        JButton editBtn = UIUtils.createRoundedButton("Edit Info", UIUtils.PRIMARY_COLOR, Color.WHITE);
        editBtn.addActionListener(e -> parent.showScreen("EDIT_INFO"));
        
        JButton imageBtn = UIUtils.createRoundedButton("Change Image", UIUtils.LIGHT_PRIMARY, UIUtils.PRIMARY_COLOR);
        imageBtn.addActionListener(e -> parent.showScreen("CHANGE_IMAGE"));

        JButton statusBtn = UIUtils.createRoundedButton("Edit Status", UIUtils.LIGHT_PRIMARY, UIUtils.PRIMARY_COLOR);
        statusBtn.addActionListener(e -> parent.showScreen("EDIT_STATUS"));

        JButton statsBtn = UIUtils.createRoundedButton("View Stats", Color.WHITE, UIUtils.TEXT_PRIMARY);
        statsBtn.addActionListener(e -> parent.showScreen("VIEW_STATS"));

        btnGrid.add(editBtn);
        btnGrid.add(imageBtn);
        btnGrid.add(statusBtn);
        btnGrid.add(statsBtn);
        card.add(btnGrid, gbc);

        add(card);

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing ViewProfileScreen after login for user: " + user.getId());
                refreshProfile();
            }
        });
    }

    public void refreshProfile() {
        System.out.println("[DEBUG] ViewProfileScreen.refreshProfile() called. Current user: " + SessionManager.getInstance().getCurrentUser());
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.out.println("[DEBUG] ViewProfileScreen.refreshProfile() aborted: No current user.");
            return;
        }

        new Thread(() -> {
            System.out.println("[DEBUG] ViewProfileScreen fetching profile for: " + currentUser.getEmail());
            User user = profileService.getProfile(currentUser.getEmail());
            System.out.println("[DEBUG] ViewProfileScreen fetched profile: " + user);
            if (user != null) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("[DEBUG] ViewProfileScreen updating UI with profile data.");
                    nameLabel.setText(user.getFullName());
                    emailLabel.setText(user.getEmail());
                    statusLabel.setText(user.getStatus() != null ? user.getStatus() : "Hey there! I'm using GECT Connect.");
                    departmentLabel.setText(user.getDepartment() != null ? user.getDepartment() : "Not specified");
                    rollNoLabel.setText("ID: " + (user.getRollNoEmpId() != null ? user.getRollNoEmpId() : "—"));
                    setProfilePicture(user.getProfilePic());
                });
            }
        }).start();
    }

    private void setProfilePicture(String path) {
        try {
            if (path == null || path.isEmpty() || !new File(path).exists()) {
                profilePicLabel.setIcon(Icons.get("user_avatar", 120));
                return;
            }
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            profilePicLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            profilePicLabel.setIcon(Icons.get("user_avatar", 120));
        }
    }
}
