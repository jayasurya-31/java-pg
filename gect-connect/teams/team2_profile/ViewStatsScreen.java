package teams.team2_profile;

import core.User;
import java.awt.*;
import javax.swing.*;
import shared.*;
import teams.team3_contacts.ContactsService;
import teams.team5_group.GroupChatService;

/**
 * GUI screen to show comprehensive profile statistics and completion percentage.
 */
public class ViewStatsScreen extends JPanel {
    private final ProfileModulePanel parent;
    private final ProfileService profileService;
    private final ContactsService contactsService;
    private final GroupChatService groupChatService;
    private final JPanel statsContainer;

    public ViewStatsScreen(ProfileModulePanel parent, ProfileService profileService) {
        this.parent = parent;
        this.profileService = profileService;
        this.contactsService = new ContactsService();
        this.groupChatService = new GroupChatService();
        
        setLayout(new BorderLayout());
        UIUtils.stylePanel(this);

        add(createHeaderLabel("Your Profile Statistics"), BorderLayout.NORTH);

        statsContainer = new JPanel();
        statsContainer.setLayout(new BoxLayout(statsContainer, BoxLayout.Y_AXIS));
        statsContainer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        UIUtils.stylePanel(statsContainer);

        JScrollPane scrollPane = new JScrollPane(statsContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JButton backBtn = UIUtils.createRoundedButton("Back to Profile", UIUtils.PRIMARY_COLOR, Color.WHITE);
        backBtn.setPreferredSize(new Dimension(0, 50));
        backBtn.addActionListener(e -> parent.showScreen("VIEW_PROFILE"));
        add(backBtn, BorderLayout.SOUTH);
    }

    /**
     * Fetch data asynchronously from services and update stats UI.
     */
    public void refreshStats() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        new Thread(() -> {
            try {
                User liveUser = profileService.getProfile(currentUser.getEmail());
                int acceptedContacts = contactsService.getContacts().size();
                int sentRequests = contactsService.getSentRequests().size();
                int blockedUsers = contactsService.getBlockedUsers().size();
                int groupsCount = groupChatService.getUserGroups().size();

                SwingUtilities.invokeLater(() -> {
                    statsContainer.removeAll();
                    if (liveUser != null) {
                        statsContainer.add(createStatRow("Accepted Contacts", acceptedContacts, "People you are connected with."));
                        statsContainer.add(Box.createVerticalStrut(10));

                        statsContainer.add(createStatRow("Sent Requests", sentRequests, "Pending connection requests you've sent."));
                        statsContainer.add(Box.createVerticalStrut(10));

                        statsContainer.add(createStatRow("Blocked Users", blockedUsers, "Users you have restricted from contacting you."));
                        statsContainer.add(Box.createVerticalStrut(10));

                        statsContainer.add(createStatRow("Groups", groupsCount, "Number of group chats you belong to."));
                        statsContainer.add(Box.createVerticalStrut(10));

                        statsContainer.add(createStatRow("Profile Created On", liveUser.getCreatedDate(), "The date you joined GECT Connect."));
                        statsContainer.add(Box.createVerticalStrut(20));

                        // ---- FIXED PROFILE COMPLETION ----
                        int completion = calculateProfileCompletion(liveUser);

                        JProgressBar progress = new JProgressBar(0, 100);
                        progress.setValue(completion);
                        progress.setStringPainted(true);
                        progress.setForeground(UIUtils.PRIMARY_COLOR);
                        progress.setPreferredSize(new Dimension(300, 30));
                        progress.setToolTipText("Complete your profile details to reach 100%.");

                        statsContainer.add(createStatRow("Profile Completion", progress, "Percentage of your profile information filled."));
                    }

                    statsContainer.revalidate();
                    statsContainer.repaint();
                });
            } catch (Exception ex) {
                DebugLogger.error("STATS → Error refreshing statistics: " + ex.getMessage());
            }
        }).start();
    }

    /**
     * Dynamically calculate profile completion percentage.
     * Only counts truly null or empty string fields.
     */
    private int calculateProfileCompletion(User user) {
        int filled = 0;
        int total = 0;

        // List of fields to check for profile completion
        String[] fields = {
            user.getFullName(),
            user.getRollNoEmpId(),
            user.getDepartment(),
            user.getMobile(),
            user.getProfilePic(),
            user.getStatus()
        };

        total = fields.length;

        for (String f : fields) {
            if (f != null && !f.trim().isEmpty()) filled++;
        }

        return (int)((filled / (double) total) * 100);
    }

    private JPanel createStatRow(String label, Object value, String tooltip) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setOpaque(false);
        row.setToolTipText(tooltip);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 16));
        row.add(lbl, BorderLayout.WEST);

        if (value instanceof Integer || value instanceof String) {
            JLabel valLbl = new JLabel(String.valueOf(value));
            valLbl.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 16));
            valLbl.setForeground(UIUtils.PRIMARY_COLOR);
            row.add(valLbl, BorderLayout.EAST);
        } else if (value instanceof JProgressBar) {
            row.add((JProgressBar) value, BorderLayout.CENTER);
        }

        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));

        return row;
    }

    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel("  " + text);
        label.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, UIUtils.HEADING_M));
        label.setPreferredSize(new Dimension(0, 60));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR));
        return label;
    }
}