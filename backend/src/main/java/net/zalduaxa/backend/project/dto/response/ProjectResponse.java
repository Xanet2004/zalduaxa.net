package net.zalduaxa.backend.project.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import net.zalduaxa.backend.project.model.Project;

public class ProjectResponse {

    private Integer id;
    private String name;
    private String slug;
    private String description;
    private String version;
    private JsonNode metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse() {}

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.slug = project.getSlug();
        this.description = project.getDescription();
        this.version = project.getVersion();
        this.metadata = project.getMetadata();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public void setMetadata(JsonNode metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
