package net.zalduaxa.backend.model.requestProjectType;

public class RequestProjectType {

    private Integer id;
    private String name;
    private String description;
    private String imagePath;

    public RequestProjectType() {}

    public RequestProjectType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public RequestProjectType(String name, String description, String imagePath) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
