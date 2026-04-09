package teams.team2_profile;

import core.User;
import shared.*;
import javax.swing.*;
import java.awt.*;

/**
 * Main panel for Team 2: Profile Management Module.
 * Manages navigation between ViewProfileScreen, EditProfileScreen, ChangeImageScreen, EditStatusScreen, and ViewStatsScreen.
 */
public class ProfileModulePanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final ProfileService profileService;
    
    private final ViewProfileScreen viewProfileScreen;
    private final EditProfileScreen editProfileScreen;
    private final ChangeImageScreen changeImageScreen;
    private final EditStatusScreen editStatusScreen;
    private final ViewStatsScreen viewStatsScreen;

    public ProfileModulePanel() {
        this.profileService = new ProfileService();
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        UIUtils.stylePanel(container);

        // Initialize screens
        viewProfileScreen = new ViewProfileScreen(this, profileService);
        editProfileScreen = new EditProfileScreen(this, profileService);
        changeImageScreen = new ChangeImageScreen(this, profileService);
        editStatusScreen = new EditStatusScreen(this, profileService);
        viewStatsScreen = new ViewStatsScreen(this, profileService);

        // Add screens to container
        container.add(viewProfileScreen, "VIEW_PROFILE");
        container.add(editProfileScreen, "EDIT_INFO");
        container.add(changeImageScreen, "CHANGE_IMAGE");
        container.add(editStatusScreen, "EDIT_STATUS");
        container.add(viewStatsScreen, "VIEW_STATS");

        add(container, BorderLayout.CENTER);

        // Event handling
        setupEventSubscriptions();

        EventBus.getInstance().subscribe("INIT_MODULES_AFTER_LOGIN", (data) -> {
            if (data instanceof User) {
                User user = (User) data;
                System.out.println("[DEBUG] Initializing ProfileModulePanel after login for user: " + user.getId());
                showScreen("VIEW_PROFILE");
            }
        });

        // Initial data load
        showScreen("VIEW_PROFILE");
    }

    /**
     * Requirement: Switches CardLayout to the given screen name.
     */
    public void showScreen(String screenName) {
        cardLayout.show(container, screenName);
        
        // Refresh data based on the screen being shown
        switch (screenName) {
            case "VIEW_PROFILE":
                viewProfileScreen.refreshProfile();
                break;
            case "EDIT_INFO":
                editProfileScreen.loadProfileData();
                break;
            case "CHANGE_IMAGE":
                changeImageScreen.loadCurrentImage();
                break;
            case "EDIT_STATUS":
                editStatusScreen.loadCurrentStatus();
                break;
            case "VIEW_STATS":
                viewStatsScreen.refreshStats();
                break;
        }
    }

    private void setupEventSubscriptions() {
        EventBus.getInstance().subscribe("PROFILE_UPDATED", data -> {
            viewProfileScreen.refreshProfile();
            editProfileScreen.loadProfileData();
        });
    }
}
