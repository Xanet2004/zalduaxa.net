package net.zalduaxa.backend.service;

import static net.zalduaxa.backend.utils.SlugUtils.slugify;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.model.project.Project;
import net.zalduaxa.backend.model.project.ProjectRepository;
import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;
import net.zalduaxa.backend.model.requestProjectType.RequestProjectType;
import net.zalduaxa.backend.model.responseProjectType.ResponseProjectType;

@Service
public class ProjectTypeService {

    private static final String ICON = "icon.png";
    private static final String PROJECT_TYPE_IMAGE_PATH = "/images/project_type.png";

    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectRepository projectRepository;
    private final Path projectTypesPath;
    private final Path projectsPath;

    public ProjectTypeService(
            ProjectTypeRepository projectTypeRepository,
            ProjectRepository projectRepository,
            @Value("${storage.path}") String storagePathStr) {
        this.projectTypeRepository = projectTypeRepository;
        this.projectRepository = projectRepository;

        Path baseStorage = resolveStoragePath(storagePathStr);
        this.projectTypesPath = baseStorage.resolve("projectTypes");
        this.projectsPath = baseStorage.resolve("projects");
    }

    public List<ResponseProjectType> getAllProjectTypes() {
        return projectTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void createProjectType(RequestProjectType request) {
        require(request != null, new BadRequestException("Request body is required"));
        require(request.getName() != null && !request.getName().isBlank(),
                new BadRequestException("Name is required"));

        require(projectTypeRepository.findByName(request.getName()) == null,
                new BadRequestException("Project Type already exists"));

        String cleanSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? slugify(request.getSlug())
                : slugify(request.getName());

        saveProjectTypeImage(cleanSlug, request.getImage());

        ProjectType projectType = new ProjectType(
                request.getName(),
                request.getDescription(),
                cleanSlug);

        projectTypeRepository.save(projectType);
    }

    public void deleteProjectType(String name) {
        require(name != null && !name.isBlank(), new BadRequestException("Name is required"));

        ProjectType projectType = projectTypeRepository.findByName(name);
        require(projectType != null, new BadRequestException("Project type not found"));

        for (Project project : projectRepository.findByProjectTypeSlug(projectType.getSlug())) {
            deleteProject(project, projectType);
        }

        deleteProjectTypeFolder(projectType.getSlug());
        projectTypeRepository.deleteById(projectType.getId());
    }

    private void deleteProject(Project project, ProjectType projectType) {
        if (project.getTypeId() == null || !project.getTypeId().equals(projectType.getId())) {
            throw new RuntimeException("Cannot delete project");
        }

        deleteProjectFolder(slugify(projectType.getSlug()), slugify(project.getSlug()));
        projectRepository.deleteById(project.getId());
    }

    private ResponseProjectType toResponse(ProjectType projectType) {
        ResponseProjectType response = new ResponseProjectType();
        response.setId(projectType.getId());
        response.setName(projectType.getName());
        response.setDescription(projectType.getDescription());
        return response;
    }

    private void saveProjectTypeImage(String projectTypeSlug, MultipartFile image) {
        Path folder = projectTypesPath.resolve(projectTypeSlug).normalize();

        saveImage(folder, ICON, image, PROJECT_TYPE_IMAGE_PATH);
    }

    private void saveImage(
            Path targetFolder,
            String fileName,
            MultipartFile image,
            String defaultClasspathImage) {
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

    private void deleteProjectTypeFolder(String storagePath) {
        Path dirProjectType = safeResolve(projectTypesPath, storagePath);
        Path dirProjects = safeResolve(projectsPath, storagePath);

        deleteTree(dirProjectType);
        deleteTree(dirProjects);
    }

    private void deleteProjectFolder(String typeSlug, String projectSlug) {
        Path dir = safeResolve(projectsPath, Paths.get(typeSlug, projectSlug).toString());
        deleteTree(dir);
    }

    private Path safeResolve(Path base, String relative) {
        Path normalizedBase = base.toAbsolutePath().normalize();
        Path resolved = normalizedBase.resolve(relative).normalize();

        if (!resolved.startsWith(normalizedBase)) {
            throw new IllegalArgumentException("Invalid path (traversal): " + relative);
        }

        return resolved;
    }

    private void deleteTree(Path dir) {
        if (Files.notExists(dir)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path path : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete: " + dir, e);
        }
    }

    private Path resolveStoragePath(String storagePathStr) {
        if (storagePathStr == null || storagePathStr.isBlank()) {
            return Paths.get("./storage").toAbsolutePath().normalize();
        }

        try {
            URI uri = URI.create(storagePathStr);
            if (uri.getScheme() != null) {
                return Paths.get(uri).toAbsolutePath().normalize();
            }
        } catch (Exception ignored) {
            // Fall back to normal filesystem path.
        }

        return Paths.get(storagePathStr).toAbsolutePath().normalize();
    }

    private static void require(boolean condition, RuntimeException ex) {
        if (!condition) {
            throw ex;
        }
    }
}