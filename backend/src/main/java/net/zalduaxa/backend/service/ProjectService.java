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
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.project.Project;
import net.zalduaxa.backend.model.project.ProjectRepository;
import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;
import net.zalduaxa.backend.model.responseProject.ResponseProject;
import net.zalduaxa.backend.model.user.User;

@Service
public class ProjectService {

    private static final String ICON = "icon.png";
    private static final String PROJECT_IMAGE_PATH = "/images/project.png";

    private final ProjectRepository projectRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final Path projectsPath;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectTypeRepository projectTypeRepository,
            @Value("${storage.path}") String storagePathStr) {
        this.projectRepository = projectRepository;
        this.projectTypeRepository = projectTypeRepository;

        Path baseStorage = resolveStoragePath(storagePathStr);
        this.projectsPath = baseStorage.resolve("projects");
    }

    public List<ResponseProject> getProjectsByType(String slug) {
        String cleanSlug = slugify(slug);

        return projectRepository.findByProjectTypeSlug(cleanSlug)
                .stream()
                .map(ResponseProject::new)
                .toList();
    }

    public ResponseProject getProjectBySlug(String slug) {
        String cleanSlug = slugify(slug);

        Project project = projectRepository.findBySlug(cleanSlug)
                .orElseThrow(() -> new BadRequestException("Project not found"));

        return new ResponseProject(project);
    }

    public void createProject(
            User user,
            String typeSlug,
            String name,
            String slug,
            String description,
            MultipartFile image) {

        if (user == null || user.getId() == null) {
            throw new UnauthorizedException("Invalid user");
        }

        require(typeSlug != null && !typeSlug.isBlank(), new BadRequestException("Type slug is required"));
        require(name != null && !name.isBlank(), new BadRequestException("Name is required"));

        String cleanTypeSlug = slugify(typeSlug);

        Optional<ProjectType> projectType = projectTypeRepository.findBySlug(cleanTypeSlug);
        require(projectType.isPresent(), new BadRequestException("Project type not found"));

        String cleanProjectSlug = (slug != null && !slug.isBlank())
                ? slugify(slug)
                : slugify(name);

        require(!projectRepository.existsBySlug(cleanProjectSlug),
                new BadRequestException("Project slug already exists"));

        Project project = new Project(
                1,
                user.getId(),
                projectType.get().getId(),
                name,
                cleanProjectSlug,
                description,
                null);

        projectRepository.save(project);
        saveProjectImage(cleanTypeSlug, cleanProjectSlug, image);
    }

    public void deleteProject(String name, String typeSlug) {
        require(name != null && !name.isBlank(), new BadRequestException("Name is required"));
        require(typeSlug != null && !typeSlug.isBlank(), new BadRequestException("Type slug is required"));

        Optional<Project> project = projectRepository.findByName(name);
        require(project.isPresent(), new BadRequestException("Project not found"));

        String cleanTypeSlug = slugify(typeSlug);

        Optional<ProjectType> projectType = projectTypeRepository.findBySlug(cleanTypeSlug);
        require(projectType.isPresent(), new BadRequestException("Project type not found"));

        deleteProject(project.get(), projectType.get());
    }

    private void deleteProject(Project project, ProjectType projectType) {
        if (project.getTypeId() == null || !project.getTypeId().equals(projectType.getId())) {
            throw new RuntimeException("Cannot delete project");
        }

        deleteProjectFolder(slugify(projectType.getSlug()), slugify(project.getSlug()));
        projectRepository.deleteById(project.getId());
    }

    private void saveProjectImage(String typeSlug, String projectSlug, MultipartFile image) {
        Path folder = projectsPath.resolve(typeSlug).resolve(projectSlug).normalize();

        saveImage(folder, ICON, image, PROJECT_IMAGE_PATH);
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