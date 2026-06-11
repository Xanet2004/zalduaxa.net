package net.zalduaxa.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.zalduaxa.backend.dto.request.DeleteProjectRequest;
import net.zalduaxa.backend.dto.request.ProjectTypeRequest;
import net.zalduaxa.backend.dto.response.MessageResponse;
import net.zalduaxa.backend.dto.response.ProjectResponse;
import net.zalduaxa.backend.dto.response.ProjectTypeResponse;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.ForbiddenException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;
import net.zalduaxa.backend.service.ProjectService;
import net.zalduaxa.backend.service.ProjectTypeService;
import net.zalduaxa.backend.service.SessionService;

@RestController
@RequestMapping("/project")
@CrossOrigin(origins = "${app.cors.origin}", allowCredentials = "true", maxAge = 3600)
@Validated
public class ProjectController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final ProjectTypeService projectTypeService;
    private final ProjectService projectService;

    public ProjectController(
            AuthService authService,
            SessionService sessionService,
            ProjectTypeService projectTypeService,
            ProjectService projectService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.projectTypeService = projectTypeService;
        this.projectService = projectService;
    }

    @GetMapping(value = "/projectTypes", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectTypeResponse>> getProjectTypes() {
        return new ResponseEntity<>(projectTypeService.getAllProjectTypes(), HttpStatus.OK);
    }

    @PostMapping(value = "/addProjectType", consumes = "multipart/form-data", produces = {
            "application/json",
            "application/xml"
    })
    public ResponseEntity<?> addProjectType(
            @Valid ProjectTypeRequest projectTypeRequest,
            HttpServletRequest request) {

        try {
            User user = requireUser(request);
            requireValidSession(user);
            requireAdmin(user);

            projectTypeService.createProjectType(projectTypeRequest);

            return ResponseEntity.ok(new MessageResponse("Project successfully created"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    @PostMapping(value = "/deleteProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectType(
            @Valid @RequestBody ProjectTypeRequest requestProjectType,
            HttpServletRequest request) {

        try {
            User user = requireUser(request);
            requireValidSession(user);

            if ("admin".equals(user.getRole().getName())) {
                String projectTypeName = requestProjectType != null ? requestProjectType.getName() : null;
                projectTypeService.deleteProjectType(projectTypeName);

                return ResponseEntity.ok(new MessageResponse("Project type successfully deleted"));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("You need to be admin to delete a new project type"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    @GetMapping("/projects/{slug}")
    public Map<String, Object> getProjectsByType(@PathVariable @NotBlank String slug) {
        List<ProjectResponse> projects = projectService.getProjectsByType(slug);

        return Map.of("projects", projects);
    }

    @GetMapping("/getProject/{slug}")
    public ResponseEntity<ProjectResponse> getProjectBySlug(@PathVariable @NotBlank String slug) {
        return ResponseEntity.ok(projectService.getProjectBySlug(slug));
    }

    @PostMapping(value = "/addProject", consumes = "multipart/form-data", produces = {
            "application/json",
            "application/xml"
    })
    public ResponseEntity<?> addProject(
            @RequestParam("typeSlug") @NotBlank(message = "Type slug is required") String typeSlug,
            @RequestParam("name") @NotBlank(message = "Name is required") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) {

        try {
            User user = requireUser(request);
            requireValidSession(user);
            requireAdmin(user);

            projectService.createProject(user, typeSlug, name, slug, description, image);

            return ResponseEntity.ok(new MessageResponse("Project successfully created"));

        } catch (BadRequestException | UnauthorizedException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Unexpected server error"));
        }
    }

    @PostMapping(value = "/deleteProject", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProject(
            @Valid @RequestBody DeleteProjectRequest body,
            HttpServletRequest request) {

        try {
            User user = requireUser(request);
            requireValidSession(user);
            requireAdmin(user);

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

    private User requireUser(HttpServletRequest request) {
        User user = authService.getUserFromRequest(request);
        if (user == null) {
            throw new UnauthorizedException("Invalid user");
        }

        return user;
    }

    private void requireValidSession(User user) {
        sessionService.assertHasActiveSession(user.getId());
    }

    private void requireAdmin(User user) {
        if (!"admin".equals(user.getRole().getName())) {
            throw new ForbiddenException("You need to be admin");
        }
    }
}