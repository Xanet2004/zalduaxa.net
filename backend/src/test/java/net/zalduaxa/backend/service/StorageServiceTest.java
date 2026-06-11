package net.zalduaxa.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class StorageServiceTest {

    @TempDir
    Path tempDir;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(tempDir.toUri().toString());
    }

    @Test
    void saveProjectImage_withUploadedImage_createsFolderAndSavesFile() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "custom.png",
                "image/png",
                "fake-image-content".getBytes());

        storageService.saveProjectImage("web", "portfolio", image);

        Path savedImage = tempDir.resolve("projects").resolve("web").resolve("portfolio").resolve("icon.png");

        assertTrue(Files.exists(savedImage));
        assertDoesNotThrow(() -> assertTrue(Files.size(savedImage) > 0));
    }

    @Test
    void saveProjectTypeImage_withUploadedImage_createsFolderAndSavesFile() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "custom.png",
                "image/png",
                "fake-image-content".getBytes());

        storageService.saveProjectTypeImage("web", image);

        Path savedImage = tempDir.resolve("projectTypes").resolve("web").resolve("icon.png");

        assertTrue(Files.exists(savedImage));
        assertDoesNotThrow(() -> assertTrue(Files.size(savedImage) > 0));
    }

    @Test
    void deleteProjectFolder_existingFolder_deletesRecursively() throws Exception {
        Path folder = tempDir.resolve("projects").resolve("web").resolve("portfolio");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("icon.png"), "fake-image-content");
        Files.createDirectories(folder.resolve("nested"));
        Files.writeString(folder.resolve("nested").resolve("file.txt"), "nested-content");

        storageService.deleteProjectFolder("web", "portfolio");

        assertFalse(Files.exists(folder));
    }

    @Test
    void deleteProjectFolder_missingFolder_doesNothing() {
        assertDoesNotThrow(() -> storageService.deleteProjectFolder("web", "missing-project"));
    }

    @Test
    void deleteProjectTypeFolder_existingFolders_deletesTypeAndProjectsFolders() throws Exception {
        Path typeFolder = tempDir.resolve("projectTypes").resolve("web");
        Path projectsFolder = tempDir.resolve("projects").resolve("web");

        Files.createDirectories(typeFolder);
        Files.writeString(typeFolder.resolve("icon.png"), "fake-type-image");

        Files.createDirectories(projectsFolder.resolve("portfolio"));
        Files.writeString(projectsFolder.resolve("portfolio").resolve("icon.png"), "fake-project-image");

        storageService.deleteProjectTypeFolder("web");

        assertFalse(Files.exists(typeFolder));
        assertFalse(Files.exists(projectsFolder));
    }

    @Test
    void deleteProjectFolder_pathTraversalInTypeSlug_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> storageService.deleteProjectFolder("../evil", "portfolio"));
    }

    @Test
    void deleteProjectFolder_pathTraversalInProjectSlug_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> storageService.deleteProjectFolder("web", "../evil"));
    }

    @Test
    void deleteProjectTypeFolder_pathTraversal_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> storageService.deleteProjectTypeFolder("../evil"));
    }

    @Test
    void constructor_nullStoragePath_usesDefaultStoragePath() {
        assertDoesNotThrow(() -> new StorageService(null));
    }

    @Test
    void constructor_blankStoragePath_usesDefaultStoragePath() {
        assertDoesNotThrow(() -> new StorageService(" "));
    }

    @Test
    void constructor_plainFilesystemPath_isAccepted() {
        assertDoesNotThrow(() -> new StorageService(tempDir.toString()));
    }
}