package net.zalduaxa.backend.dto.response;

import net.zalduaxa.backend.model.role.Role;

public class RoleResponse {

    private Integer id;
    private String name;
    private String description;

    public RoleResponse() {}

    public RoleResponse(Role role) {
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
