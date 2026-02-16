package net.zalduaxa.backend.model.requestProjectType;

import org.springframework.web.multipart.MultipartFile;

public class RequestProjectType {

    private String name;
    private String slug;
    private String description;
    private MultipartFile image;

    public RequestProjectType() {}

    public RequestProjectType(String name, String slug, String description, MultipartFile image) {
        this.name = name;
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
