package net.zalduaxa.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DeleteProjectRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Type slug is required")
    @Size(max = 255, message = "Type slug must be at most 255 characters")
    private String typeSlug;

    public DeleteProjectRequest() {}

    public DeleteProjectRequest(String name, String typeSlug) {
        this.name = name;
        this.typeSlug = typeSlug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeSlug() {
        return typeSlug;
    }

    public void setTypeSlug(String typeSlug) {
        this.typeSlug = typeSlug;
    }
}
