package net.zalduaxa.backend.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;
import net.zalduaxa.backend.model.requestProjectType.RequestProjectType;
import net.zalduaxa.backend.model.session.Session;
import net.zalduaxa.backend.model.session.SessionRepository;
import net.zalduaxa.backend.model.user.User;
import net.zalduaxa.backend.service.AuthService;
import net.zalduaxa.backend.service.JwtService;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true", maxAge = 3600)
@RestController
@RequestMapping("/project")
public class ProjectController {

    private static final String STORAGE_PATH = "C:\\Users\\xanet\\Work\\web\\zalduaxa.net\\storage";
    private static final String PROJECT_TYPES_PATH = STORAGE_PATH + "\\projectTypes";
    private static final String PROJECTS_PATH = STORAGE_PATH + "\\projects";

    @Autowired
    ProjectTypeRepository projectTypeRepo;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionRepository sessionRepository;

    @GetMapping(value = "/projectTypes", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectType>> getProjectTypes() {
        List<ProjectType> projectTypes = projectTypeRepo.findAll();
        return new ResponseEntity<>(projectTypes, HttpStatus.OK);
    }

    @PostMapping(value = "/addProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> addProjectType(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("storage_path") String storagePath,
            @RequestPart("image") MultipartFile image,
            HttpServletRequest request) {

        List<ProjectType> projectTypes = projectTypeRepo.findAll();
        try {
            User user = authService.getUserFromToken(extractToken(request), jwtService);
            if (user == null)
                return new ResponseEntity<>(Map.of("message", "Invalid user"), HttpStatus.UNAUTHORIZED);

            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            if (sessionOpt.isEmpty() || !sessionRepository.existsById(sessionOpt.get().getId()))
                return new ResponseEntity<>(Map.of("message", "Invalid session"), HttpStatus.BAD_REQUEST);

            if (!"admin".equals(user.getRole().getName())) 
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You need to be admin to add a new project type"));

            if (projectTypeRepo.findByName(name) != null)
                return new ResponseEntity<>(Map.of("message", "Project Type already exists"), HttpStatus.BAD_REQUEST);

            ProjectType projectType = new ProjectType();
            projectType.setName(name);
            projectType.setDescription(description);
            saveRequestImage(storagePath, image);
            String slug = !storagePath.isEmpty() ? slugify(storagePath) : slugify(name);
            projectType.setStoragePath(slug);
            projectTypeRepo.save(projectType);
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
        List<ProjectType> projectTypes = projectTypeRepo.findAll();
        try {
            User user = authService.getUserFromToken(extractToken(request), jwtService);
            if (user == null)
                return new ResponseEntity<>(Map.of("message", "Invalid user"), HttpStatus.UNAUTHORIZED);

            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            if (sessionOpt.isEmpty() || !sessionRepository.existsById(sessionOpt.get().getId()))
                return new ResponseEntity<>(Map.of("message", "Invalid session"), HttpStatus.BAD_REQUEST);

            if ("admin".equals(user.getRole().getName())) {
                for (ProjectType projectType : projectTypes) {
                    if(projectType.getName().equals(requestProjectType.getName())){
                        deleteProjectTypeFolder(projectType.getStoragePath());
                        projectTypeRepo.deleteById(projectType.getId());
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

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName()))
                    return cookie.getValue();
            }
        }
        return null;
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
}