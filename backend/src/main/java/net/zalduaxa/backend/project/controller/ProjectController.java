package net.zalduaxa.backend.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.zalduaxa.backend.project.dto.request.DeleteProjectRequest;
import net.zalduaxa.backend.project.dto.request.ProjectTypeRequest;
import net.zalduaxa.backend.common.dto.MessageResponse;
import net.zalduaxa.backend.project.dto.response.ProjectResponse;
import net.zalduaxa.backend.project.dto.response.ProjectTypeResponse;
import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.common.exception.ForbiddenException;
import net.zalduaxa.backend.common.exception.UnauthorizedException;
import net.zalduaxa.backend.auth.security.AuthenticatedUser;
import net.zalduaxa.backend.project.service.ProjectService;
import net.zalduaxa.backend.project.service.ProjectTypeService;

@RestController
@CrossOrigin(origins = "${app.cors.origin}", allowCredentials = "true", maxAge = 3600)
@Validated
public class ProjectController {

    private final ProjectTypeService projectTypeService;
    private final ProjectService projectService;

    public ProjectController(
            ProjectTypeService projectTypeService,
            ProjectService projectService) {
        this.projectTypeService = projectTypeService;
        this.projectService = projectService;
    }

    // ---------------------------------------------------------------------
    // New REST endpoints
    // ---------------------------------------------------------------------

    @GetMapping(value = "/project-types", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectTypeResponse>> getProjectTypes() {
        return getProjectTypesResponse();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/project-types", consumes = "multipart/form-data", produces = {
            "application/json",
            "application/xml"
    })
    public ResponseEntity<?> addProjectType(
            @Valid ProjectTypeRequest projectTypeRequest) {
        return createProjectTypeResponse(projectTypeRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/project-types/{slug}", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectTypeBySlug(
            @PathVariable @NotBlank String slug) {
        try {
            projectTypeService.deleteProjectTypeBySlug(slug);

            return ResponseEntity.ok(new MessageResponse("Project type successfully deleted"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    @GetMapping("/project-types/{slug}/projects")
    public Map<String, Object> getProjectsByType(@PathVariable @NotBlank String slug) {
        return getProjectsByTypeResponse(slug);
    }

    @GetMapping("/projects/{slug}")
    public ResponseEntity<ProjectResponse> getProjectBySlug(@PathVariable @NotBlank String slug) {
        return getProjectBySlugResponse(slug);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/projects", consumes = "multipart/form-data", produces = {
            "application/json",
            "application/xml"
    })
    public ResponseEntity<?> addProject(
            @RequestParam("typeSlug") @NotBlank(message = "Type slug is required") String typeSlug,
            @RequestParam("name") @NotBlank(message = "Name is required") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return createProjectResponse(typeSlug, name, slug, description, image, principal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/projects/{slug}", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectBySlug(
            @PathVariable @NotBlank String slug) {
        try {
            projectService.deleteProjectBySlug(slug);

            return ResponseEntity.ok(new MessageResponse("Project successfully deleted"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    // ---------------------------------------------------------------------
    // Deprecated legacy endpoints
    // ---------------------------------------------------------------------

    @Deprecated
    @GetMapping(value = "/project/projectTypes", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectTypeResponse>> getProjectTypesLegacy() {
        return getProjectTypesResponse();
    }

    @Deprecated
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/project/addProjectType", consumes = "multipart/form-data", produces = {
            "application/json",
            "application/xml"
    })
    public ResponseEntity<?> addProjectTypeLegacy(
            @Valid ProjectTypeRequest projectTypeRequest) {
        return createProjectTypeResponse(projectTypeRequest);
    }

    @Deprecated
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/project/deleteProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectTypeLegacy(
            @Valid @RequestBody ProjectTypeRequest requestProjectType) {

        try {
            String projectTypeName = requestProjectType != null ? requestProjectType.getName() : null;
            projectTypeService.deleteProjectType(projectTypeName);

            return ResponseEntity.ok(new MessageResponse("Project type successfully deleted"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    @Deprecated
    @GetMapping("/project/projects/{slug}")
    public Map<String, Object> getProjectsByTypeLegacy(@PathVariable @NotBlank String slug) {
        return getProjectsByTypeResponse(slug);
    }

    @Deprecated
    @GetMapping("/project/getProject/{slug}")
    public ResponseEntity<ProjectResponse> getProjectBySlugLegacy(@PathVariable @NotBlank String slug) {
        return getProjectBySlugResponse(slug);
    }

    @Deprecated
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/project/addProject", consumes = "multipart/form-data", produces = {
            "application/json",
            "application/xml"
    })
    public ResponseEntity<?> addProjectLegacy(
            @RequestParam("typeSlug") @NotBlank(message = "Type slug is required") String typeSlug,
            @RequestParam("name") @NotBlank(message = "Name is required") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return createProjectResponse(typeSlug, name, slug, description, image, principal);
    }

    @Deprecated
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/project/deleteProject", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectLegacy(
            @Valid @RequestBody DeleteProjectRequest body) {

        try {
            String projectName = body != null ? body.getName() : null;
            String typeSlug = body != null ? body.getTypeSlug() : null;

            projectService.deleteProject(projectName, typeSlug);

            return ResponseEntity.ok(new MessageResponse("Project successfully deleted"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    // ---------------------------------------------------------------------
    // Shared response helpers
    // ---------------------------------------------------------------------

    private ResponseEntity<List<ProjectTypeResponse>> getProjectTypesResponse() {
        return new ResponseEntity<>(projectTypeService.getAllProjectTypes(), HttpStatus.OK);
    }

    private ResponseEntity<?> createProjectTypeResponse(ProjectTypeRequest projectTypeRequest) {
        try {
            projectTypeService.createProjectType(projectTypeRequest);

            return ResponseEntity.ok(new MessageResponse("Project successfully created"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    private Map<String, Object> getProjectsByTypeResponse(String slug) {
        List<ProjectResponse> projects = projectService.getProjectsByType(slug);

        return Map.of("projects", projects);
    }

    private ResponseEntity<ProjectResponse> getProjectBySlugResponse(String slug) {
        return ResponseEntity.ok(projectService.getProjectBySlug(slug));
    }

    private ResponseEntity<?> createProjectResponse(
            String typeSlug,
            String name,
            String slug,
            String description,
            MultipartFile image,
            AuthenticatedUser principal) {

        try {
            Integer userId = principal != null ? principal.id() : null;

            projectService.createProject(userId, typeSlug, name, slug, description, image);

            return ResponseEntity.ok(new MessageResponse("Project successfully created"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }
}