package net.zalduaxa.backend.model.responseUser;

import net.zalduaxa.backend.model.user.User;

public class ResponseUser {

    private String username;
    private String fullName;
    private String email;
    private String profilePicture;
    private SimpleRoleResponse role;

    public ResponseUser() {}

    public ResponseUser(String username, String fullName, String email) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
    }

    public ResponseUser(User user) {
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.profilePicture = user.getProfilePicture();
        this.role = new SimpleRoleResponse(user.getRole());
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

    public SimpleRoleResponse getRole() {
        return role;
    }

    public void setRole(SimpleRoleResponse role) {
        this.role = role;
    }
}