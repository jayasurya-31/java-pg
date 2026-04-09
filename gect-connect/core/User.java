package core;

import java.sql.Timestamp;

/**
 * Global User model for GECT Connect.
 * Aligned with the new database schema.
 */
public class User {
    private int id;
    private String email;
    private String fullName;
    private String password;
    private String personType;
    private String rollNoEmpId;
    private String department;
    private String mobile;
    private String profilePic;
    private String status;
    private String headlineText;
    private boolean isOnline;
    private Timestamp lastSeen;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
    }

    public User(int id, String email, String fullName, String password, String personType, String rollNoEmpId, String department, String mobile, String profilePic, String status, boolean isOnline, Timestamp lastSeen, Timestamp createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.personType = personType;
        this.rollNoEmpId = rollNoEmpId;
        this.department = department;
        this.mobile = mobile;
        this.profilePic = profilePic;
        this.status = status;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPersonType() { return personType; }
    public void setPersonType(String personType) { this.personType = personType; }

    public String getRollNoEmpId() { return rollNoEmpId; }
    public void setRollNoEmpId(String rollNoEmpId) { this.rollNoEmpId = rollNoEmpId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHeadlineText() { return headlineText; }
    public void setHeadlineText(String headlineText) { this.headlineText = headlineText; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public Timestamp getLastSeen() { return lastSeen; }
    public void setLastSeen(Timestamp lastSeen) { this.lastSeen = lastSeen; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Compatibility methods for legacy code
    public int getUserId() { return id; }
    public void setUserId(int userId) { this.id = userId; }
    public String getRole() { return personType; }
    public void setRole(String role) { this.personType = role; }
    public String getProfilePicPath() { return profilePic; }
    public void setProfilePicPath(String profilePicPath) { this.profilePic = profilePicPath; }
    public String getBio() { return status; }
    public void setBio(String bio) { this.status = bio; }
    public Timestamp getJoinDate() { return createdAt; }

    // JSON Spec Compatibility
    public String getName() { return fullName; }
    public javax.swing.Icon getProfileImage() {
        if (profilePic != null && !profilePic.isEmpty()) {
            // For now, return a placeholder but the logic for real image can be added here
            return new javax.swing.ImageIcon(profilePic);
        }
        return null;
    }

    public void setJoinDate(Timestamp joinDate) { this.createdAt = joinDate; }
    public String getIdNumber() { return rollNoEmpId; }

    /**
     * Requirement: Compute null/empty fields for profile completion.
     */
    public int countNullFields() {
        int nullCount = 0;
        if (fullName == null || fullName.trim().isEmpty()) nullCount++;
        if (email == null || email.trim().isEmpty()) nullCount++;
        if (mobile == null || mobile.trim().isEmpty()) nullCount++;
        if (department == null || department.trim().isEmpty()) nullCount++;
        if (profilePic == null || profilePic.trim().isEmpty()) nullCount++;
        if (status == null || status.trim().isEmpty()) nullCount++;
        if (rollNoEmpId == null || rollNoEmpId.trim().isEmpty()) nullCount++;
        if (personType == null || personType.trim().isEmpty()) nullCount++;
        if (headlineText == null || headlineText.trim().isEmpty()) nullCount++;
        if (createdAt == null) nullCount++;
        return nullCount;
    }

    /**
     * Requirement: Return formatted created date string.
     */
    public String getCreatedDate() {
        if (createdAt == null) return "N/A";
        return new java.text.SimpleDateFormat("MMMM dd, yyyy").format(createdAt);
    }
}
