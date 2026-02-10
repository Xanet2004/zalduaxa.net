package net.zalduaxa.backend.model.requestProject;

import java.time.LocalDateTime;

public class RequestProject {

    private Integer id;
    private Integer storageId;
    private Integer ownerId;
    private String typeSlug;
    private Integer visibilityId;
    private Integer statusId;
    private String name;
    private String slug;
    private String description;
    private String version;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public RequestProject() {}

    public RequestProject(Integer storageId, Integer ownerId, String typeSlug, String name, String slug) {
        this.storageId = storageId;
        this.ownerId = ownerId;
        this.typeSlug = typeSlug;
        this.name = name;
        this.slug = slug;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getStorageId() { return storageId; }
    public void setStorageId(Integer storageId) { this.storageId = storageId; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public String getTypeSlug() { return typeSlug; }
    public void setTypeSlug(String typeSlug) { this.typeSlug = typeSlug; }

    public Integer getVisibilityId() { return visibilityId; }
    public void setVisibilityId(Integer visibilityId) { this.visibilityId = visibilityId; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
