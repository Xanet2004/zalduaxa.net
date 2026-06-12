package net.zalduaxa.backend.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import net.zalduaxa.backend.project.dto.request.ProjectTypeRequest;
import net.zalduaxa.backend.project.dto.response.ProjectTypeResponse;
import net.zalduaxa.backend.project.model.Project;
import net.zalduaxa.backend.project.model.ProjectRepository;
import net.zalduaxa.backend.project.model.ProjectType;
import net.zalduaxa.backend.project.model.ProjectTypeRepository;
import net.zalduaxa.backend.storage.service.StorageService;

@ExtendWith(MockitoExtension.class)
class ProjectTypeServiceTest {

    @Mock
    private ProjectTypeRepository projectTypeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private ProjectTypeService projectTypeService;

    @Test
    void getAllProjectTypes_whenRepositoryHasTypes_returnsResponses() {
        ProjectType web = projectType(1, "Web", "web");
        ProjectType recipes = projectType(2, "Recipes", "recipes");

        when(projectTypeRepository.findAll()).thenReturn(List.of(web, recipes));

        List<ProjectTypeResponse> result = projectTypeService.getAllProjectTypes();

        assertEquals(2, result.size());
        assertEquals("Web", result.get(0).getName());
        assertEquals("web", result.get(0).getSlug());
        assertEquals("Recipes", result.get(1).getName());
        assertEquals("recipes", result.get(1).getSlug());
    }

    @Test
    void getAllProjectTypes_whenRepositoryIsEmpty_returnsEmptyList() {
        when(projectTypeRepository.findAll()).thenReturn(List.of());

        List<ProjectTypeResponse> result = projectTypeService.getAllProjectTypes();

        assertEquals(List.of(), result);
    }

    @Test
    void createProjectType_whenRequestIsNull_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.createProjectType(null),
                "Request body is required"
        );

        assertEquals("Request body is required", ex.getMessage());

        verify(projectTypeRepository, never()).save(any());
        verify(storageService, never()).saveProjectTypeImage(any(), any());
    }

    @Test
    void createProjectType_whenNameIsBlank_throwsBadRequest() {
        ProjectTypeRequest request = request("   ", "web", "desc", image);

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.createProjectType(request),
                "Name is required"
        );

        assertEquals("Name is required", ex.getMessage());

        verify(projectTypeRepository, never()).save(any());
        verify(storageService, never()).saveProjectTypeImage(any(), any());
    }

    @Test
    void createProjectType_whenNameAlreadyExists_throwsBadRequest() {
        ProjectTypeRequest request = request("Web", "web", "desc", image);

        when(projectTypeRepository.findByName("Web")).thenReturn(Optional.of(projectType(1, "Web", "web")));

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.createProjectType(request),
                "Project Type already exists"
        );

        assertEquals("Project Type already exists", ex.getMessage());

        verify(projectTypeRepository, never()).save(any());
        verify(storageService, never()).saveProjectTypeImage(any(), any());
    }

    @Test
    void createProjectType_whenValidWithExplicitSlug_savesImageAndType() {
        ProjectTypeRequest request = request("Web", "Custom Slug", "desc", image);

        when(projectTypeRepository.findByName("Web")).thenReturn(Optional.empty());

        projectTypeService.createProjectType(request);

        ArgumentCaptor<ProjectType> captor = ArgumentCaptor.forClass(ProjectType.class);
        verify(projectTypeRepository).save(captor.capture());

        ProjectType saved = captor.getValue();

        assertEquals("Web", saved.getName());
        assertEquals("desc", saved.getDescription());
        assertEquals("custom-slug", saved.getSlug());

        verify(storageService).saveProjectTypeImage("custom-slug", image);
    }

    @Test
    void createProjectType_whenValidWithoutSlug_generatesSlugFromName() {
        ProjectTypeRequest request = request("My Type", null, "desc", image);

        when(projectTypeRepository.findByName("My Type")).thenReturn(Optional.empty());

        projectTypeService.createProjectType(request);

        ArgumentCaptor<ProjectType> captor = ArgumentCaptor.forClass(ProjectType.class);
        verify(projectTypeRepository).save(captor.capture());

        ProjectType saved = captor.getValue();

        assertEquals("my-type", saved.getSlug());
        verify(storageService).saveProjectTypeImage("my-type", image);
    }

    @Test
    void deleteProjectType_whenNameIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.deleteProjectType("   "),
                "Name is required"
        );

        assertEquals("Name is required", ex.getMessage());

        verify(projectTypeRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectTypeFolder(any());
    }

    @Test
    void deleteProjectType_whenTypeDoesNotExist_throwsBadRequest() {
        when(projectTypeRepository.findByName("Web")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.deleteProjectType("Web"),
                "Project type not found"
        );

        assertEquals("Project type not found", ex.getMessage());

        verify(projectTypeRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectTypeFolder(any());
    }

    @Test
    void deleteProjectType_whenTypeHasNoProjects_deletesTypeFolderAndType() {
        ProjectType type = projectType(10, "Web", "web");

        when(projectTypeRepository.findByName("Web")).thenReturn(Optional.of(type));
        when(projectRepository.findByTypeId(10)).thenReturn(List.of());

        projectTypeService.deleteProjectType("Web");

        verify(storageService).deleteProjectTypeFolder("web");
        verify(projectTypeRepository).deleteById(10);
        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectFolder(any(), any());
    }

    @Test
    void deleteProjectType_whenTypeHasProjects_deletesProjectFoldersProjectsTypeFolderAndType() {
        ProjectType type = projectType(10, "Web", "web");
        Project first = project(1, 10, "Portfolio", "portfolio");
        Project second = project(2, 10, "Blog", "blog");

        when(projectTypeRepository.findByName("Web")).thenReturn(Optional.of(type));
        when(projectRepository.findByTypeId(10)).thenReturn(List.of(first, second));

        projectTypeService.deleteProjectType("Web");

        verify(storageService).deleteProjectFolder("web", "portfolio");
        verify(storageService).deleteProjectFolder("web", "blog");
        verify(projectRepository).deleteById(1);
        verify(projectRepository).deleteById(2);
        verify(storageService).deleteProjectTypeFolder("web");
        verify(projectTypeRepository).deleteById(10);
    }

    @Test
    void deleteProjectType_whenProjectTypeDoesNotMatch_throwsRuntimeException() {
        ProjectType type = projectType(10, "Web", "web");
        Project project = project(1, 99, "Portfolio", "portfolio");

        when(projectTypeRepository.findByName("Web")).thenReturn(Optional.of(type));
        when(projectRepository.findByTypeId(10)).thenReturn(List.of(project));

        RuntimeException ex = assertThrowsWithMessage(
                RuntimeException.class,
                () -> projectTypeService.deleteProjectType("Web"),
                "Cannot delete project"
        );

        assertEquals("Cannot delete project", ex.getMessage());

        verify(projectRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectTypeFolder(any());
    }

    @Test
    void deleteProjectTypeBySlug_whenSlugIsBlank_throwsBadRequest() {
        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.deleteProjectTypeBySlug("   "),
                "Slug is required"
        );

        assertEquals("Slug is required", ex.getMessage());

        verify(projectTypeRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectTypeFolder(any());
    }

    @Test
    void deleteProjectTypeBySlug_whenTypeDoesNotExist_throwsBadRequest() {
        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrowsWithMessage(
                BadRequestException.class,
                () -> projectTypeService.deleteProjectTypeBySlug("Web"),
                "Project type not found"
        );

        assertEquals("Project type not found", ex.getMessage());

        verify(projectTypeRepository, never()).deleteById(any());
        verify(storageService, never()).deleteProjectTypeFolder(any());
    }

    @Test
    void deleteProjectTypeBySlug_whenValid_deletesTypeBySlug() {
        ProjectType type = projectType(10, "Web", "web");

        when(projectTypeRepository.findBySlug("web")).thenReturn(Optional.of(type));
        when(projectRepository.findByTypeId(10)).thenReturn(List.of());

        projectTypeService.deleteProjectTypeBySlug("Web");

        verify(storageService).deleteProjectTypeFolder("web");
        verify(projectTypeRepository).deleteById(10);
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

    private ProjectTypeRequest request(String name, String slug, String description, MultipartFile image) {
        ProjectTypeRequest request = new ProjectTypeRequest();
        request.setName(name);
        request.setSlug(slug);
        request.setDescription(description);
        request.setImage(image);
        return request;
    }

    private Project project(Integer id, Integer typeId, String name, String slug) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", id);
        project.setTypeId(typeId);
        project.setName(name);
        project.setSlug(slug);
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