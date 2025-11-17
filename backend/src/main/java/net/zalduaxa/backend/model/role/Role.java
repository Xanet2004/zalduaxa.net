package net.zalduaxa.backend.model.role;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Optional: Relation with User
    // @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    // private Set<User> users = new HashSet<>();

    // Constructors
    public Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // public Set<User> getUsers() {
    //     return users;
    // }

    // public void setUsers(Set<User> users) {
    //     this.users = users;
    // }

    // // Helper methods
    // public void addUser(User user) {
    //     users.add(user);
    //     user.setRole(this);
    // }

    // public void removeUser(User user) {
    //     users.remove(user);
    //     user.setRole(null);
    // }
}
