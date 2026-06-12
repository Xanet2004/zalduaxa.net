package net.zalduaxa.backend.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.common.exception.UnauthorizedException;
import net.zalduaxa.backend.project.dto.response.ProjectResponse;
import net.zalduaxa.backend.project.model.Project;
import net.zalduaxa.backend.project.model.ProjectRepository;
import net.zalduaxa.backend.project.model.ProjectType;
import net.zalduaxa.backend.project.model.ProjectTypeRepository;
import net.zalduaxa.backend.storage.service.StorageService;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectTypeRepository projectTypeRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void getProjectsByType_whenTypeExists_returnsProjectsForType() {
        ProjectType type = projectType(10, "Web", "web");
        Project first = project(1, 10, "Portfolio", "portfolio");
        Project second = project(2, 10, "Blog", "blog");

        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));
        when(projectRepository.findByTypeId(10)).thenReturn(List.of(first, second));

        List<ProjectResponse> result = projectService.getProjectsByType("Web");

        assertEquals(2, result.size());
        assertEquals("Portfolio", result.get(0).getName());
        assertEquals("portfolio", result.get(0).getSlug());
        assertEquals("Blog", result.get(1).getName());
        assertEquals("blog", result.get(1).getSlug());

        verify(projectTypeRepository).findBySlug("web");
        verify(projectRepository).findByTypeId(10);
    }

    @Test
    void getProjectsByType_whenTypeDoesNotExist_returnsEmptyList() {
        when(projectTypeRepository.findBySlug("missing-type")).thenReturn(Optional.empty());

        List<ProjectResponse> result = projectService.getProjectsByType("Missing Type");

        assertEquals(List.of(), result);

        verify(projectTypeRepository).findBySlug("missing-type");
        verify(projectRepository, never()).findByTypeId(any());
    }

    @Test
    void getProjectBySlug_whenProjectExists_returnsProject() {
        Project project = project(1, 10, "Portfolio", "portfolio");

        when(projectRepository.findBySlug("portfolio")).thenReturn(Optional.of(project));

        ProjectResponse result = projectService.getProjectBySlug("Portfolio");

        assertEquals(1, result.getId());
        assertEquals("Portfolio", result.getName());
        assertEquals("portfolio", result.getSlug());
    }

    @Test
    void getProjectBySlug_whenProjectDoesNotExist_throwsBadRequest() {
        when(projectRepository.findBySlug("missing")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.getProjectBySlug("Missing"),
                "Project not found"
        );

        assertEquals("Project not found", ex.getMessage());
    }

    @Test
    void createProject_whenUserIdIsNull_throwsUnauthorized() {
        UnauthorizedException ex = assertThrowsWithMessage(
                UnauthorizedException.class,
                () -> projectService.createProject(null, "web", "Portfolio", "portfolio", "desc", image),
                "Invalid user"
        );

        assertEquals("Invalid user", ex.getMessage());

        verify(projectRepository, never()).save(any());
        verify(storageService, never()).saveProjectImage(any(), any(), any());
    }

    @Test
    void createProject_whenTypeSlugIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.createProject(1, "   ", "Portfolio", "portfolio", "desc", image),
                "Type slug is required"
        );

        assertEquals("Type slug is required", ex.getMessage());

        verify(projectRepository, never()).save(any());
        verify(storageService, never()).saveProjectImage(any(), any(), any());
    }

    @Test
    void createProject_whenNameIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.createProject(1, "web", "   ", "portfolio", "desc", image),
                "Name is required"
        );

        assertEquals("Name is required", ex.getMessage());

        verify(projectRepository, never()).save(any());
        verify(storageService, never()).saveProjectImage(any(), any(), any());
    }

    @Test
    void createProject_whenTypeDoesNotExist_throwsBadRequest() {
        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.createProject(1, "web", "Portfolio", "portfolio", "desc", image),
                "Project type not found"
        );

        assertEquals("Project type not found", ex.getMessage());

        verify(projectRepository, never()).save(any());
        verify(storageService, never()).saveProjectImage(any(), any(), any());
    }

    @Test
    void createProject_whenSlugAlreadyExists_throwsBadRequest() {
        ProjectType type = projectType(10, "Web", "web");

        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));
        when(projectRepository.existsBySlug("portfolio")).thenReturn(true);

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.createProject(1, "web", "Portfolio", "portfolio", "desc", image),
                "Project slug already exists"
        );

        assertEquals("Project slug already exists", ex.getMessage());

        verify(projectRepository, never()).save(any());
        verify(storageService, never()).saveProjectImage(any(), any(), any());
    }

    @Test
    void createProject_whenValidWithExplicitSlug_savesProjectAndImage() {
        ProjectType type = projectType(10, "Web", "web");

        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));
        when(projectRepository.existsBySlug("custom-slug")).thenReturn(false);

        projectService.createProject(5, "web", "Portfolio", "Custom Slug", "desc", image);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project saved = projectCaptor.getValue();

        assertEquals(1, saved.getStorageId());
        assertEquals(5, saved.getOwnerId());
        assertEquals(10, saved.getTypeId());
        assertEquals("Portfolio", saved.getName());
        assertEquals("custom-slug", saved.getSlug());
        assertEquals("desc", saved.getDescription());

        verify(storageService).saveProjectImage("web", "custom-slug", image);
    }

    @Test
    void createProject_whenValidWithoutSlug_generatesSlugFromName() {
        ProjectType type = projectType(10, "Web", "web");

        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));
        when(projectRepository.existsBySlug("my-portfolio")).thenReturn(false);

        projectService.createProject(5, "web", "My Portfolio", null, "desc", image);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project saved = projectCaptor.getValue();

        assertEquals("my-portfolio", saved.getSlug());
        verify(storageService).saveProjectImage("web", "my-portfolio", image);
    }

    @Test
    void deleteProject_whenNameIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProject("   ", "web"),
                "Name is required"
        );

        assertEquals("Name is required", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProject_whenTypeSlugIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProject("Portfolio", "   "),
                "Type slug is required"
        );

        assertEquals("Type slug is required", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProject_whenProjectDoesNotExist_throwsBadRequest() {
        when(projectRepository.findByName("Portfolio")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProject("Portfolio", "web"),
                "Project not found"
        );

        assertEquals("Project not found", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProject_whenProjectTypeDoesNotExist_throwsBadRequest() {
        Project project = project(1, 10, "Portfolio", "portfolio");

        when(projectRepository.findByName("Portfolio")).thenReturn(Optional.of(project));
        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProject("Portfolio", "web"),
                "Project type not found"
        );

        assertEquals("Project type not found", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProject_whenProjectTypeDoesNotMatch_throwsRuntimeException() {
        Project project = project(1, 10, "Portfolio", "portfolio");
        ProjectType type = projectType(99, "Web", "web");

        when(projectRepository.findByName("Portfolio")).thenReturn(Optional.of(project));
        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));

        RuntimeException ex = assertThrowsWithMessage(
                RuntimeException.class,
                () -> projectService.deleteProject("Portfolio", "web"),
                "Cannot delete project"
        );

        assertEquals("Cannot delete project", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProject_whenValid_deletesStorageFolderAndProject() {
        Project project = project(1, 10, "Portfolio", "portfolio");
        ProjectType type = projectType(10, "Web", "web");

        when(projectRepository.findByName("Portfolio")).thenReturn(Optional.of(project));
        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));

        projectService.deleteProject("Portfolio", "web");

        verify(storageService).deleteProjectFolder("web", "portfolio");
        verify(projectRepository).deleteById(1);
    }

    @Test
    void deleteProjectBySlug_whenSlugIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProjectBySlug("   "),
                "Slug is required"
        );

        assertEquals("Slug is required", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProjectBySlug_whenProjectDoesNotExist_throwsBadRequest() {
        when(projectRepository.findBySlug("portfolio")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProjectBySlug("Portfolio"),
                "Project not found"
        );

        assertEquals("Project not found", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProjectBySlug_whenProjectTypeDoesNotExist_throwsBadRequest() {
        Project project = project(1, 10, "Portfolio", "portfolio");

        when(projectRepository.findBySlug("portfolio")).thenReturn(Optional.of(project));
        when(projectTypeRepository.findById(10)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectService.deleteProjectBySlug("Portfolio"),
                "Project type not found"
        );

        assertEquals("Project type not found", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProjectBySlug_whenValid_deletesStorageFolderAndProject() {
        Project project = project(1, 10, "Portfolio", "portfolio");
        ProjectType type = projectType(10, "Web", "web");

        when(projectRepository.findBySlug("portfolio")).thenReturn(Optional.of(project));
        when(projectTypeRepository.findById(10)).thenReturn(Optional.of(type));

        projectService.deleteProjectBySlug("Portfolio");

        verify(storageService).deleteProjectFolder("web", "portfolio");
        verify(projectRepository).deleteById(1);
    }

    private <T extends Throwable> T assertThrowsWithMessage(
            Class<T> expectedType,
            Executable executable,
            String expectedMessage) {
        T exception = assertThrows(expectedType, executable);
        assertSame(expectedType, exception.getClass());
        assertEquals(expectedMessage, exception.getMessage());
        return exception;
    }

    private Project project(Integer id, Integer typeId, String name, String slug) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", id);
        project.setTypeId(typeId);
        project.setName(name);
        project.setSlug(slug);
        project.setDescription(name + " description");
        return project;
    }

    private ProjectType projectType(Integer id, String name, String slug) {
        ProjectType type = new ProjectType();
        ReflectionTestUtils.setField(type, "id", id);
        type.setName(name);
        type.setSlug(slug);
        type.setDescription(name + " description");
        return type;
    }
}