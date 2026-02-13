package net.zalduaxa.backend.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    ProjectTypeRepository projectTypeRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private SessionRepository sessionRepository;

    public ProjectController(@Value("${storage.path}") String storagePathStr) {
        Path base = Paths.get(java.net.URI.create(storagePathStr));
        this.STORAGE_PATH = base.toAbsolutePath().toString();
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
            @RequestParam("name") String name,
            @RequestParam("slug") String slug,
            @RequestParam("description") String description,
            @RequestPart("image") MultipartFile image,
            HttpServletRequest request) {

        List<ProjectType> projectTypes = projectTypeRepository.findAll();
        try {
            User user = authService.getUserFromRequest(request);
            if (user == null)
                return new ResponseEntity<>(Map.of("message", "Invalid user"), HttpStatus.UNAUTHORIZED);

            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            if (sessionOpt.isEmpty() || !sessionRepository.existsById(sessionOpt.get().getId()))
                return new ResponseEntity<>(Map.of("message", "Invalid session"), HttpStatus.BAD_REQUEST);

            if (!"admin".equals(user.getRole().getName())) 
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You need to be admin to add a new project type"));

            if (projectTypeRepository.findByName(name) != null)
                return new ResponseEntity<>(Map.of("message", "Project Type already exists"), HttpStatus.BAD_REQUEST);

            slug = !slug.isEmpty() ? slugify(slug) : slugify(name);
            ProjectType projectType = new ProjectType(name, description, slug);
            
            saveRequestImage(slug, image);
            projectTypeRepository.save(projectType);
            
            return ResponseEntity.ok(Map.of("message", "Project successfully created"));

        } catch (Exception e) {
            return new ResponseEntity<>(projectTypes, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Boolean saveRequestImage(String folderName, MultipartFile image) {
        try {
            File folder = new File(PROJECT_TYPES_PATH, folderName);
            if (!folder.exists() && !folder.mkdirs()) {
                throw new RuntimeException("Cannot create folder " + folder.getAbsolutePath());
            }
            File destination = new File(folder, "icon.png");
            image.transferTo(destination);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    @PostMapping(value = "/deleteProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectType(@RequestBody RequestProjectType requestProjectType,
            HttpServletResponse response, HttpServletRequest request) {
        List<ProjectType> projectTypes = projectTypeRepository.findAll();
        try {
            User user = authService.getUserFromRequest(request);
            if (user == null)
                return new ResponseEntity<>(Map.of("message", "Invalid user"), HttpStatus.UNAUTHORIZED);

            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            if (sessionOpt.isEmpty() || !sessionRepository.existsById(sessionOpt.get().getId()))
                return new ResponseEntity<>(Map.of("message", "Invalid session"), HttpStatus.BAD_REQUEST);

            if ("admin".equals(user.getRole().getName())) {
                for (ProjectType projectType : projectTypes) {
                    if(projectType.getName().equals(requestProjectType.getName())){
                        deleteProjectTypeFolder(projectType.getSlug());
                        projectTypeRepository.deleteById(projectType.getId());
                    }
                }
                return ResponseEntity.ok(Map.of("message", "Project type successfully deleted"));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You need to be admin to delete a new project type"));

        } catch (Exception e) {
            return new ResponseEntity<>(projectTypes, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteProjectTypeFolder(String storagePath) {
        java.nio.file.Path dir = java.nio.file.Paths.get(PROJECT_TYPES_PATH + '\\' + storagePath);
        if (!java.nio.file.Files.exists(dir)) return;
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { java.nio.file.Files.delete(p); } catch (java.io.IOException e) { throw new RuntimeException(e); }
            });
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }

    @GetMapping("/projects/{slug}")
    public Map<String, Object> getProjectsByType(@PathVariable String slug) {
        String cleanSlug = slugify(slug);
        var projects = projectRepository.findByProjectTypeSlug(cleanSlug);
        return Map.of("projects", projects);
    }

    public static String slugify(String input) {
        String text = input.toLowerCase();
        text = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        text = text.replaceAll("\\s+", "-");
        text = text.replaceAll("[^a-z0-9-_]", "");
        text = text.replaceAll("-{2,}", "-");
        text = text.replaceAll("^-|-$", "");
        return text;
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
    public ResponseEntity<?> addProject(
            @RequestParam("typeSlug") String typeSlug,
            @RequestParam("name") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) {

        try {
            User user = authService.getUserFromRequest(request);
            if (user == null)
                return new ResponseEntity<>(Map.of("message", "Invalid user"), HttpStatus.UNAUTHORIZED);

            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            if (sessionOpt.isEmpty() || !sessionRepository.existsById(sessionOpt.get().getId()))
                return new ResponseEntity<>(Map.of("message", "Invalid session"), HttpStatus.BAD_REQUEST);

            if (!"admin".equals(user.getRole().getName()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You need to be admin to add a new project"));

            String cleanTypeSlug = slugify(typeSlug);
            Optional<ProjectType> pt = projectTypeRepository.findBySlug(cleanTypeSlug);
            if (pt == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project type not found"));

            if (name == null || name.isBlank())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Name is required"));

            String cleanProjectSlug = (slug != null && !slug.isBlank()) ? slugify(slug) : slugify(name);

            if (projectRepository.existsBySlug(cleanProjectSlug))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Project slug already exists"));

            Project p = new Project();
            p.setName(name);
            p.setSlug(cleanProjectSlug);
            p.setDescription(description);
            p.setTypeId(projectTypeRepository.findBySlug(cleanTypeSlug).get().getId());
            p.setOwnerId(user.getId());
            p.setStorageId(1);
            p.setMetadata(null);

            projectRepository.save(p);

            if (image != null && !image.isEmpty()) {
                saveProjectImage(cleanTypeSlug, cleanProjectSlug, image);
            }

            return ResponseEntity.ok(Map.of("message", "Project successfully created"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected server error"));
        }
    }

    private Boolean saveProjectImage(String typeSlug, String projectSlug, MultipartFile image) {
        try {
            File folder = new File(PROJECTS_PATH + "\\" + typeSlug, projectSlug);
            if (!folder.exists() && !folder.mkdirs()) {
                throw new RuntimeException("Cannot create folder " + folder.getAbsolutePath());
            }
            File destination = new File(folder, "icon.png");
            image.transferTo(destination);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    @PostMapping(value = "/deleteProject", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProject(
            @RequestBody net.zalduaxa.backend.model.requestProject.RequestProject body,
            HttpServletRequest request) {

        try {
            User user = authService.getUserFromRequest(request);
            if (user == null)
                return new ResponseEntity<>(Map.of("message", "Invalid user"), HttpStatus.UNAUTHORIZED);

            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            if (sessionOpt.isEmpty() || !sessionRepository.existsById(sessionOpt.get().getId()))
                return new ResponseEntity<>(Map.of("message", "Invalid session"), HttpStatus.BAD_REQUEST);

            if (!"admin".equals(user.getRole().getName()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You need to be admin to delete a project"));

            if (body.getName() == null || body.getName().isBlank())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "name is required"));

            Optional<Project> pOpt = projectRepository.findByName(body.getName());
            if (pOpt.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));

            if (body.getTypeSlug() == null || body.getTypeSlug().isBlank())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "typeSlug is required"));

            String cleanTypeSlug = slugify(body.getTypeSlug());
            String cleanProjectSlug = slugify(pOpt.get().getSlug());

            Optional<ProjectType> pt = projectTypeRepository.findBySlug(cleanTypeSlug);
            if (pt == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project type not found"));

            Project p = pOpt.get();
            if (p.getTypeId() == null || !p.getTypeId().equals(pt.get().getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));

            deleteProjectFolder(cleanTypeSlug, cleanProjectSlug);
            projectRepository.deleteById(p.getId());

            return ResponseEntity.ok(Map.of("message", "Project successfully deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Unexpected server error"));
        }
    }

    private void deleteProjectFolder(String typeSlug, String projectSlug) {
        java.nio.file.Path dir = java.nio.file.Paths.get(PROJECTS_PATH + '\\' + typeSlug + '\\' + projectSlug);
        if (!java.nio.file.Files.exists(dir)) return;
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try { java.nio.file.Files.delete(p); } catch (java.io.IOException e) { throw new RuntimeException(e); }
            });
        } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }


}