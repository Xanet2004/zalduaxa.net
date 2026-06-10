package net.zalduaxa.backend.model.responseUser;

import net.zalduaxa.backend.model.role.Role;

public class SimpleRoleResponse {

    private Integer id;
    private String name;
    private String description;

    public SimpleRoleResponse() {}

    public SimpleRoleResponse(Role role) {
        if (role != null) {
            this.id = role.getId();
            this.name = role.getName();
            this.description = role.getDescription();
        }
    }

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
}