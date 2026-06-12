package net.zalduaxa.backend.project.dto.response;

import net.zalduaxa.backend.project.model.ProjectType;

public class ProjectTypeResponse {

    private Integer id;
    private String name;
    private String slug;
    private String description;

    public ProjectTypeResponse() {}

    public ProjectTypeResponse(ProjectType projectType) {
        this.id = projectType.getId();
        this.name = projectType.getName();
        this.slug = projectType.getSlug();
        this.description = projectType.getDescription();
    }

    public ProjectTypeResponse(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
