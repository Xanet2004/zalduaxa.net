package net.zalduaxa.backend.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.ForbiddenException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.project.Project;
import net.zalduaxa.backend.model.project.ProjectRepository;
import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;
import net.zalduaxa.backend.model.requestProjectType.RequestProjectType;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;

@RestController
@RequestMapping("/project")
@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true",
    maxAge = 3600
)
public class ProjectController {

    // TODO: BASE PATH FROM STORAGE ON DDBB
    private String STORAGE_PATH;
    private String PROJECT_TYPES_PATH;
    private String PROJECTS_PATH;
    private String icon = "icon.png";
    private String IMAGES_PATH = "/images/";
    private String PROJECT_TYPE_IMAGE_PATH = IMAGES_PATH + "project_type.png";
    private String PROJECT_IMAGE_PATH = IMAGES_PATH + "project.png";

    @Autowired
    ProjectTypeRepository projectTypeRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionRepository sessionRepository;

    public ProjectController(@Value("${storage.path}") String storagePathStr) {
        Path base_storage = Paths.get(java.net.URI.create(storagePathStr));
        this.STORAGE_PATH = base_storage.toAbsolutePath().toString();
        this.PROJECT_TYPES_PATH = STORAGE_PATH + "\\projectTypes";
        this.PROJECTS_PATH = STORAGE_PATH + "\\projects";
    }


    @GetMapping(value = "/projectTypes", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectType>> getProjectTypes() {
        List<ProjectType> projectTypes = projectTypeRepository.findAll();
        return new ResponseEntity<>(projectTypes, HttpStatus.OK);
    }

    @PostMapping(
        value = "/addProjectType",
        consumes = "multipart/form-data",
        produces = { "application/json", "application/xml" }
    )
    public ResponseEntity<?> addProjectType(
            RequestProjectType projectTypeRequest,
            HttpServletRequest request) {

        try {
            // * Check requisites
            User user = requireUser(request);
            requireValidSession(user);
            requireAdmin(user);
            require(projectTypeRequest.getName() != null && !projectTypeRequest.getName().isBlank(), new BadRequestException("Name is required"));
            require(projectTypeRepository.findByName(projectTypeRequest.getName()) == null, new BadRequestException("Project Type already exists"));
            
            // * Confirm slug
            String cleanSlug = (projectTypeRequest.getSlug() != null && !projectTypeRequest.getSlug().isBlank())
                                ? slugify(projectTypeRequest.getSlug())
                                : slugify(projectTypeRequest.getName());
            
            // * Save image
            saveRequestImage(cleanSlug, projectTypeRequest.getImage());

            // * Create and save projectType
            ProjectType projectType = new ProjectType(projectTypeRequest.getName(), projectTypeRequest.getDescription(), cleanSlug);
            projectTypeRepository.save(projectType);

            return ResponseEntity.ok(Map.of("message", "Project successfully created"));

        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Error creating project type: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // TODO: Optimise save request (project type) image and save project image
    // ? -> Sum both methods to avoid redundant code
    private void saveRequestImage(String projectTypeSlug, MultipartFile image) {
        Path folder = Paths.get(PROJECT_TYPES_PATH).resolve(projectTypeSlug).normalize();
        try {
            Files.createDirectories(folder);
            Path destination = folder.resolve("icon.png");

            if (image != null && !image.isEmpty()) {
                image.transferTo(destination.toFile());
                return;
            }

            ClassPathResource defaultIcon = new ClassPathResource(PROJECT_TYPE_IMAGE_PATH);
            try (InputStream in = defaultIcon.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save icon", e);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/deleteProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectType(@RequestBody RequestProjectType requestProjectType,
            HttpServletResponse response, HttpServletRequest request) {

        List<ProjectType> projectTypes = projectTypeRepository.findAll();
        try {
            // * Check requisites
            User user = requireUser(request);
            requireValidSession(user);

            // TODO: Turn this into a method for more readability
            if ("admin".equals(user.getRole().getName())) {
                for (ProjectType projectType : projectTypes) 
                    if(projectType.getName().equals(requestProjectType.getName())) 
                        deleteProjectType(projectType);
                return ResponseEntity.ok(Map.of("message", "Project type successfully deleted"));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You need to be admin to delete a new project type"));

        } catch (Exception e) {
            return new ResponseEntity<>(projectTypes, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteProjectType(ProjectType projectType){
        for (Project project : projectRepository.findByProjectTypeSlug(projectType.getSlug())) {
            deleteProject(project, projectType);
        }
        deleteProjectTypeFolder(projectType.getSlug());
        projectTypeRepository.deleteById(projectType.getId());
    }

    private void deleteProjectTypeFolder(String storagePath) {
        Path dirProjectType = safeResolve(Paths.get(PROJECT_TYPES_PATH), storagePath);
        Path dirProjects = safeResolve(Paths.get(PROJECTS_PATH),        storagePath);

        deleteTree(dirProjectType);
        deleteTree(dirProjects);
    }

    private void deleteTree(Path dir) {
        if (Files.notExists(dir)) return;

        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete: " + dir, e);
        }
    }

    private Path safeResolve(Path base, String relative) {
        Path b = base.toAbsolutePath().normalize();
        Path r = b.resolve(relative).normalize();

        if (!r.startsWith(b)) {
            throw new IllegalArgumentException("Invalid path (traversal): " + relative);
        }
        return r;
    }

    @GetMapping("/projects/{slug}")
    public Map<String, Object> getProjectsByType(@PathVariable String slug) {
        // TODO: Check if required authentication steps are needed
        // * Get clean slug
        String cleanSlug = slugify(slug);

        // * Get projects
        var projects = projectRepository.findByProjectTypeSlug(cleanSlug);

        // * Return projects
        return Map.of("projects", projects);
    }

    private static String slugify(String input) {
        if (input == null) return "untitled";

        String text = input.trim().toLowerCase(Locale.ROOT);

        text = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        text = text.replaceAll("[^a-z0-9_-]+", "-");
        text = text.replaceAll("^-+|-+$", "");

        return text.isEmpty() ? "untitled" : text;
    }

    @GetMapping("/getProject/{slug}")
    public ResponseEntity<?> getProjectBySlug(@PathVariable String slug) {
        String cleanSlug = slugify(slug);
        Optional<Project> project = projectRepository.findBySlug(cleanSlug);
        if (project.isPresent()) {
            return ResponseEntity.ok(project.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));
        }
    }

    @PostMapping(
        value = "/addProject",
        consumes = "multipart/form-data",
        produces = { "application/json", "application/xml" }
    )
    // TODO: Shorter method header, requestProject with multipart file?
    public ResponseEntity<?> addProject(
            @RequestParam("typeSlug") String typeSlug,
            @RequestParam("name") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) {

        try {
            // * Check requisites
            User user = requireUser(request);
            requireValidSession(user);
            requireAdmin(user);

            // * Check project type
            String cleanTypeSlug = slugify(typeSlug);
            Optional<ProjectType> pt = projectTypeRepository.findBySlug(cleanTypeSlug);
            require(pt == null, new BadRequestException("Project type not found"));
            require(name == null || name.isBlank(), new BadRequestException("Name is required"));

            // * Check project
            String cleanProjectSlug = (slug != null && !slug.isBlank()) ? slugify(slug) : slugify(name);
            require(projectRepository.existsBySlug(cleanProjectSlug), new BadRequestException("Project slug already exists"));

            // * Create and save project
            Project p = new Project(1, user.getId(), projectTypeRepository.findBySlug(cleanTypeSlug).get().getId(), name, cleanProjectSlug, description, null);            
            projectRepository.save(p);
            saveProjectImage(cleanTypeSlug, cleanProjectSlug, image);

            return ResponseEntity.ok(Map.of("message", "Project successfully created"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected server error"));
        }
    }

    // TODO: Optimise save request (project type) image and save project image
    // ? -> Sum both methods to avoid redundant code
    private void saveProjectImage(
            String typeSlug,
            String projectSlug,
            MultipartFile image
    ) {
        Path folder = Paths.get(PROJECTS_PATH).resolve(typeSlug).resolve(projectSlug).normalize();

        saveImage(folder, icon, image, PROJECT_IMAGE_PATH);
    }

    private void saveImage(
            Path targetFolder,
            String fileName,
            MultipartFile image,
            String defaultClasspathImage
    ) {
        try {
            Files.createDirectories(targetFolder);
            Path destination = targetFolder.resolve(fileName);

            if (image != null && !image.isEmpty()) {
                image.transferTo(destination.toFile());
                return;
            }
            
            ClassPathResource defaultImage = new ClassPathResource(defaultClasspathImage);
            try (InputStream in = defaultImage.getInputStream()) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to save image", e);
        }
    }

    @PostMapping(value = "/deleteProject", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProject(
            @RequestBody net.zalduaxa.backend.model.requestProject.RequestProject body,
            HttpServletRequest request) {

        // TODO: Delete project just with slug

        try {
            // * Check user requisites
            User user = requireUser(request);
            requireValidSession(user);
            requireAdmin(user);
            
            // * Check project requisites
            require(body.getName() == null || body.getName().isBlank(), new BadRequestException("Name is required"));
            Optional<Project> pOpt = projectRepository.findByName(body.getName());
            require(pOpt.isEmpty(), new BadRequestException("Project not found"));
            require(body.getTypeSlug() == null || body.getTypeSlug().isBlank(), new BadRequestException("Type slug is required"));

            String cleanTypeSlug = slugify(body.getTypeSlug());

            // * Check project type requisites
            ProjectType pt = projectTypeRepository.findBySlug(cleanTypeSlug).get();
            require(pt == null, new BadRequestException("Project type not found"));

            // * Delete project
            Project p = pOpt.get();
            deleteProject(p, pt);

            return ResponseEntity.ok(Map.of("message", "Project successfully deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected server error"));
        }
    }

    private void deleteProject(Project p, ProjectType pt){
        require(p.getTypeId() != null || p.getTypeId().equals(pt.getId()), new RuntimeException("Cannot delete project"));
        deleteProjectFolder(slugify(pt.getSlug()), slugify(p.getSlug()));
        projectRepository.deleteById(p.getId());
    }

    private void deleteProjectFolder(String typeSlug, String projectSlug) {
        Path base = Paths.get(PROJECTS_PATH);
        Path dir = safeResolve(base, Paths.get(typeSlug, projectSlug).toString());
        deleteTree(dir);
    }

    private User requireUser(HttpServletRequest request) {
        User user = authService.getUserFromRequest(request);
        if (user == null) throw new UnauthorizedException("Invalid user");
        return user;
    }

    private Session requireValidSession(User user) {
        return sessionRepository.findByUserId(user.getId().longValue())
            .filter(s -> sessionRepository.existsById(s.getId()))
            .orElseThrow(() -> new UnauthorizedException("Invalid session"));
    }

    private void requireAdmin(User user) {
        if (!"admin".equals(user.getRole().getName())) {
            throw new ForbiddenException("You need to be admin");
        }
    }

    private static void require(boolean condition, RuntimeException ex) {
        if (!condition) throw ex;
    }


}