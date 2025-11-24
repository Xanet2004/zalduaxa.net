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

@CrossOrigin(
    origins = "http://localhost:5173",
    allowCredentials = "true",
    maxAge = 3600
)
@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    ProjectTypeRepository projectTypeRepo;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SessionRepository sessionRepository;

    @GetMapping(value = "/projectTypes", produces = { "application/json", "application/xml" })
    public ResponseEntity<List<ProjectType>> getProjectTypes(){
        List<ProjectType> projectTypes = projectTypeRepo.findAll();

        return new ResponseEntity<>(projectTypes, HttpStatus.OK);
    }

    @PostMapping(value = "/addProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> addProjectType(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestPart("image") MultipartFile image,
        HttpServletRequest request) {

        List<ProjectType> projectTypes = projectTypeRepo.findAll();
        ProjectType projectType = new ProjectType();
        try {
            User user = authService.getUserFromToken(extractToken(request), jwtService);
            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            // ? Check user has a session
            if(!sessionRepository.existsById(sessionOpt.get().getId())) return new ResponseEntity<>("The project is not created", HttpStatus.BAD_REQUEST);

            if(user.getRole().getName().equals("admin")){
                projectType.setName(name);
                projectType.setDescription(description);
                projectType.setImagePath(saveRequestImage(name, image));
                projectTypeRepo.save(projectType);
                return ResponseEntity.ok(Map.of("message", "Project succesfully created"));
            }

            return ResponseEntity.ok(Map.of("message", "You need to be admin to add a new project type"));

        } catch (Exception e) {
            return new ResponseEntity<>(projectTypes, HttpStatus.OK);
        }
    }

    private String saveRequestImage(String projectTypeName, MultipartFile image) {
        String folderPath = "C:\\Users\\xanet\\Work\\web\\zalduaxa.net\\storage\\projectTypes" + "\\" + slugify(projectTypeName);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = "icon.png"; 
        File destination = new File(folderPath + "\\" + fileName);

        try {
            image.transferTo(destination);
            return "projectTypes/" + projectTypeName + '/' + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping(value = "/deleteProjectType", produces = { "application/json", "application/xml" })
    public ResponseEntity<?> deleteProjectType(@RequestBody RequestProjectType projectTypeName, HttpServletResponse response, HttpServletRequest request){
        List<ProjectType> projectTypes = projectTypeRepo.findAll();
        try {
            User user = authService.getUserFromToken(extractToken(request), jwtService);
            Optional<Session> sessionOpt = sessionRepository.findByUserId(user.getId().longValue());
            // ? Check user has a session
            if(!sessionRepository.existsById(sessionOpt.get().getId())) return new ResponseEntity<>(projectTypes, HttpStatus.OK);

            if(user.getRole().getName().equals("admin")){
                deleteProjectTypeImage(projectTypeName.getName(), projectTypeRepo.findByName(projectTypeName.getName()).getImagePath());
                projectTypeRepo.deleteById(projectTypeRepo.findByName(projectTypeName.getName()).getId());
                return ResponseEntity.ok(Map.of("message", "Project type succesfully deleted"));
            }

            return ResponseEntity.ok(Map.of("message", "You need to be admin to delete a new project type"));

        } catch (Exception e) {
            return new ResponseEntity<>(projectTypes, HttpStatus.OK);
        }
    }

    private Boolean deleteProjectTypeImage(String projectTypeName, String imagePath) {
        String folderPath = "C:\\Users\\xanet\\Work\\web\\zalduaxa.net\\storage\\projectTypes" + "\\" + slugify(projectTypeName);
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = "icon.png"; 
        File file = new File(folderPath + "\\" + fileName);

        file.delete();
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("token")) {
                    return cookie.getValue();
                }
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
