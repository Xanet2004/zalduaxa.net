package net.zalduaxa.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final String ICON = "icon.png";
    private static final String PROJECT_TYPE_IMAGE_PATH = "/images/project_type.png";
    private static final String PROJECT_IMAGE_PATH = "/images/project.png";

    private final Path projectTypesPath;
    private final Path projectsPath;

    public StorageService(@Value("${storage.path}") String storagePathStr) {
        Path baseStorage = resolveStoragePath(storagePathStr);
        this.projectTypesPath = baseStorage.resolve("projectTypes");
        this.projectsPath = baseStorage.resolve("projects");
    }

    public void saveProjectTypeImage(String projectTypeSlug, MultipartFile image) {
        Path folder = safeResolve(projectTypesPath, projectTypeSlug);

        saveImage(folder, ICON, image, PROJECT_TYPE_IMAGE_PATH);
    }

    public void saveProjectImage(String typeSlug, String projectSlug, MultipartFile image) {
        Path typeFolder = safeResolve(projectsPath, typeSlug);
        Path projectFolder = safeResolve(typeFolder, projectSlug);

        saveImage(projectFolder, ICON, image, PROJECT_IMAGE_PATH);
    }


    public void deleteProjectTypeFolder(String projectTypeSlug) {
        Path dirProjectType = safeResolve(projectTypesPath, projectTypeSlug);
        Path dirProjects = safeResolve(projectsPath, projectTypeSlug);

        deleteTree(dirProjectType);
        deleteTree(dirProjects);
    }

    public void deleteProjectFolder(String typeSlug, String projectSlug) {
        Path typeFolder = safeResolve(projectsPath, typeSlug);
        Path projectFolder = safeResolve(typeFolder, projectSlug);

        deleteTree(projectFolder);
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
}