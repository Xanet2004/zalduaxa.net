package net.zalduaxa.backend.model.requestProjectType;

import org.springframework.web.multipart.MultipartFile;

public class RequestProjectType {

    private Integer id;
    private String name;
    private String storagePath;
    private String description;
    private MultipartFile image;

    public RequestProjectType() {}

    public RequestProjectType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public RequestProjectType(String name, String description, MultipartFile image) {
        this.name = name;
        this.description = description;
        this.image = image;
    }

    // Getters and Setters

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

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
