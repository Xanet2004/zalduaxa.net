package net.zalduaxa.backend.auth.dto.response;

import net.zalduaxa.backend.auth.model.User;

public class UserResponse {

    private String username;
    private String fullName;
    private String email;
    private String profilePicture;
    private RoleResponse role;

    public UserResponse() {}

    public UserResponse(String username, String fullName, String email) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }

    public UserResponse(User user) {
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.profilePicture = user.getProfilePicture();
        this.role = new RoleResponse(user.getRole());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public RoleResponse getRole() {
        return role;
    }

    public void setRole(RoleResponse role) {
        this.role = role;
    }
}
