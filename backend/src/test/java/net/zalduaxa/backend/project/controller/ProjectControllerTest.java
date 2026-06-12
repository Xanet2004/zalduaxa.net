package net.zalduaxa.backend.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import net.zalduaxa.backend.auth.security.AuthenticatedUser;
import net.zalduaxa.backend.auth.security.JwtAuthenticationFilter;
import net.zalduaxa.backend.common.exception.ApiExceptionHandler;
import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.project.dto.request.ProjectTypeRequest;
import net.zalduaxa.backend.project.dto.response.ProjectResponse;
import net.zalduaxa.backend.project.dto.response.ProjectTypeResponse;
import net.zalduaxa.backend.project.model.Project;
import net.zalduaxa.backend.project.model.ProjectType;
import net.zalduaxa.backend.project.service.ProjectService;
import net.zalduaxa.backend.project.service.ProjectTypeService;

@WebMvcTest(ProjectController.class)
@Import(ApiExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "app.cors.origin=http://localhost:5173"
})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectTypeService projectTypeService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProjectTypes_returnsProjectTypes() throws Exception {
        when(projectTypeService.getAllProjectTypes())
                .thenReturn(List.of(projectTypeResponse(1, "Web", "web")));

        mockMvc.perform(get("/project-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Web"))
                .andExpect(jsonPath("$[0].slug").value("web"));

        verify(projectTypeService).getAllProjectTypes();
    }

    @Test
    void addProjectType_whenValidMultipart_returnsOk() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "icon.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes());

        mockMvc.perform(multipart("/project-types")
                .file(image)
                .param("name", "Web")
                .param("slug", "web")
                .param("description", "Web projects")
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project successfully created"));

        verify(projectTypeService).createProjectType(any(ProjectTypeRequest.class));
    }

    @Test
    void addProjectType_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        doThrow(new BadRequestException("Project Type already exists"))
                .when(projectTypeService).createProjectType(any(ProjectTypeRequest.class));

        mockMvc.perform(multipart("/project-types")
                .param("name", "Web")
                .param("slug", "web")
                .with(adminAuthentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Project Type already exists"));
    }

    @Test
    void addProjectType_whenServiceThrowsUnexpectedException_returnsInternalServerErrorMessage() throws Exception {
        doThrow(new RuntimeException("Unexpected storage error"))
                .when(projectTypeService).createProjectType(any(ProjectTypeRequest.class));

        mockMvc.perform(multipart("/project-types")
                .param("name", "Web")
                .param("slug", "web")
                .with(adminAuthentication()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error"));
    }

    @Test
    void deleteProjectTypeBySlug_whenValid_returnsOk() throws Exception {
        mockMvc.perform(delete("/project-types/web")
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project type successfully deleted"));

        verify(projectTypeService).deleteProjectTypeBySlug("web");
    }

    @Test
    void deleteProjectTypeBySlug_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        doThrow(new BadRequestException("Project type not found"))
                .when(projectTypeService).deleteProjectTypeBySlug("missing");

        mockMvc.perform(delete("/project-types/missing")
                .with(adminAuthentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Project type not found"));
    }

    @Test
    void deleteProjectTypeBySlug_whenServiceThrowsUnexpectedException_returnsInternalServerErrorMessage()
            throws Exception {
        doThrow(new RuntimeException("Unexpected delete error"))
                .when(projectTypeService).deleteProjectTypeBySlug("web");

        mockMvc.perform(delete("/project-types/web")
                .with(adminAuthentication()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error"));
    }

    @Test
    void getProjectsByType_returnsProjectsWrappedInProjectsKey() throws Exception {
        when(projectService.getProjectsByType("web"))
                .thenReturn(List.of(projectResponse(1, "Portfolio", "portfolio")));

        mockMvc.perform(get("/project-types/web/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[0].id").value(1))
                .andExpect(jsonPath("$.projects[0].name").value("Portfolio"))
                .andExpect(jsonPath("$.projects[0].slug").value("portfolio"));

        verify(projectService).getProjectsByType("web");
    }

    @Test
    void getProjectBySlug_returnsProject() throws Exception {
        when(projectService.getProjectBySlug("portfolio"))
                .thenReturn(projectResponse(1, "Portfolio", "portfolio"));

        mockMvc.perform(get("/projects/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Portfolio"))
                .andExpect(jsonPath("$.slug").value("portfolio"));

        verify(projectService).getProjectBySlug("portfolio");
    }

    @Test
    void getProjectBySlug_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        when(projectService.getProjectBySlug("missing"))
                .thenThrow(new BadRequestException("Project not found"));

        mockMvc.perform(get("/projects/missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void addProject_whenValidMultipartAndPrincipal_returnsOk() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "project.png",
                MediaType.IMAGE_PNG_VALUE,
                "image-content".getBytes());

        mockMvc.perform(multipart("/projects")
                .file(image)
                .param("typeSlug", "web")
                .param("name", "Portfolio")
                .param("slug", "portfolio")
                .param("description", "Portfolio description")
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project successfully created"));

        verify(projectService).createProject(
                eq(1),
                eq("web"),
                eq("Portfolio"),
                eq("portfolio"),
                eq("Portfolio description"),
                any());
    }

    @Test
    void addProject_whenPrincipalIsMissing_delegatesNullUserIdAndReturnsBadRequestFromHandler() throws Exception {
        doThrow(new BadRequestException("Invalid user"))
                .when(projectService)
                .createProject(eq(null), eq("web"), eq("Portfolio"), eq("portfolio"), eq(null), any());

        mockMvc.perform(multipart("/projects")
                .param("typeSlug", "web")
                .param("name", "Portfolio")
                .param("slug", "portfolio"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid user"));
    }

    @Test
    void addProject_whenServiceThrowsUnexpectedException_returnsInternalServerErrorMessage() throws Exception {
        doThrow(new RuntimeException("Unexpected create error"))
                .when(projectService)
                .createProject(eq(1), eq("web"), eq("Portfolio"), eq("portfolio"), eq(null), any());

        mockMvc.perform(multipart("/projects")
                .param("typeSlug", "web")
                .param("name", "Portfolio")
                .param("slug", "portfolio")
                .with(adminAuthentication()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error"));
    }

    @Test
    void deleteProjectBySlug_whenValid_returnsOk() throws Exception {
        mockMvc.perform(delete("/projects/portfolio")
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project successfully deleted"));

        verify(projectService).deleteProjectBySlug("portfolio");
    }

    @Test
    void deleteProjectBySlug_whenServiceThrowsBadRequest_returnsBadRequest() throws Exception {
        doThrow(new BadRequestException("Project not found"))
                .when(projectService).deleteProjectBySlug("missing");

        mockMvc.perform(delete("/projects/missing")
                .with(adminAuthentication()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void deleteProjectBySlug_whenServiceThrowsUnexpectedException_returnsInternalServerErrorMessage() throws Exception {
        doThrow(new RuntimeException("Unexpected delete error"))
                .when(projectService).deleteProjectBySlug("portfolio");

        mockMvc.perform(delete("/projects/portfolio")
                .with(adminAuthentication()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected server error"));
    }

    @Test
    void getProjectTypesLegacy_returnsProjectTypes() throws Exception {
        when(projectTypeService.getAllProjectTypes())
                .thenReturn(List.of(projectTypeResponse(1, "Web", "web")));

        mockMvc.perform(get("/project/projectTypes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Web"));

        verify(projectTypeService).getAllProjectTypes();
    }

    @Test
    void getProjectsByTypeLegacy_returnsProjectsWrappedInProjectsKey() throws Exception {
        when(projectService.getProjectsByType("web"))
                .thenReturn(List.of(projectResponse(1, "Portfolio", "portfolio")));

        mockMvc.perform(get("/project/projects/web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[0].name").value("Portfolio"));

        verify(projectService).getProjectsByType("web");
    }

    @Test
    void getProjectBySlugLegacy_returnsProject() throws Exception {
        when(projectService.getProjectBySlug("portfolio"))
                .thenReturn(projectResponse(1, "Portfolio", "portfolio"));

        mockMvc.perform(get("/project/getProject/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Portfolio"));

        verify(projectService).getProjectBySlug("portfolio");
    }

    @Test
    void deleteProjectTypeLegacy_whenValid_returnsOk() throws Exception {
        mockMvc.perform(post("/project/deleteProjectType")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Web"
                        }
                        """)
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project type successfully deleted"));

        verify(projectTypeService).deleteProjectType("Web");
    }

    @Test
    void addProjectLegacy_whenValid_returnsOk() throws Exception {
        mockMvc.perform(multipart("/project/addProject")
                .param("typeSlug", "web")
                .param("name", "Portfolio")
                .param("slug", "portfolio")
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project successfully created"));

        verify(projectService).createProject(
                eq(1),
                eq("web"),
                eq("Portfolio"),
                eq("portfolio"),
                eq(null),
                any());
    }

    @Test
    void deleteProjectLegacy_whenValid_returnsOk() throws Exception {
        mockMvc.perform(post("/project/deleteProject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Portfolio",
                          "typeSlug": "web"
                        }
                        """)
                .with(adminAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project successfully deleted"));

        verify(projectService).deleteProject("Portfolio", "web");
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor adminAuthentication() {
        AuthenticatedUser principal = new AuthenticatedUser(
                1,
                "admin",
                "admin@example.com",
                "ADMIN");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        return request -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    private ProjectResponse projectResponse(Integer id, String name, String slug) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", id);
        project.setName(name);
        project.setSlug(slug);
        project.setDescription(name + " description");
        return new ProjectResponse(project);
    }

    private ProjectTypeResponse projectTypeResponse(Integer id, String name, String slug) {
        ProjectType projectType = new ProjectType();
        ReflectionTestUtils.setField(projectType, "id", id);
        projectType.setName(name);
        projectType.setSlug(slug);
        projectType.setDescription(name + " description");
        return new ProjectTypeResponse(projectType);
    }
}