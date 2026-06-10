package net.zalduaxa.backend.model.requestProjectType;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RequestProjectType {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Pattern(
        regexp = "^$|^[a-zA-Z0-9_-]+$",
        message = "Slug can only contain letters, numbers, underscores and hyphens"
    )
    @Size(max = 255, message = "Slug must be at most 255 characters")
    private String slug;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private MultipartFile image;

    public RequestProjectType() {}

    public RequestProjectType(String name, String slug, String description, MultipartFile image) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.image = image;
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

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}