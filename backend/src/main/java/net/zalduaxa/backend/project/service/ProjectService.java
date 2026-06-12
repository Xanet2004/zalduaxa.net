package net.zalduaxa.backend.project.service;

import static net.zalduaxa.backend.common.util.SlugUtils.slugify;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.zalduaxa.backend.project.dto.response.ProjectResponse;
import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.common.exception.UnauthorizedException;
import net.zalduaxa.backend.project.model.Project;
import net.zalduaxa.backend.project.model.ProjectRepository;
import net.zalduaxa.backend.project.model.ProjectType;
import net.zalduaxa.backend.project.model.ProjectTypeRepository;
import net.zalduaxa.backend.storage.service.StorageService;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final StorageService storageService;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectTypeRepository projectTypeRepository,
            StorageService storageService) {
        this.projectRepository = projectRepository;
        this.projectTypeRepository = projectTypeRepository;
        this.storageService = storageService;
    }

    public List<ProjectResponse> getProjectsByType(String slug) {
        String cleanSlug = slugify(slug);

        return projectTypeRepository.findBySlug(cleanSlug)
                .map(projectType -> projectRepository.findByTypeId(projectType.getId())
                        .stream()
                        .map(ProjectResponse::new)
                        .toList())
                .orElseGet(List::of);
    }

    public ProjectResponse getProjectBySlug(String slug) {
        String cleanSlug = slugify(slug);

        Project project = projectRepository.findBySlug(cleanSlug)
                .orElseThrow(() -> new BadRequestException("Project not found"));

        return new ProjectResponse(project);
    }

    public void createProject(
            Integer userId,
            String typeSlug,
            String name,
            String slug,
            String description,
            MultipartFile image) {

        if (userId == null) {
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
                userId,
                projectType.get().getId(),
                name,
                cleanProjectSlug,
                description,
                null);

        projectRepository.save(project);
        storageService.saveProjectImage(cleanTypeSlug, cleanProjectSlug, image);
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

    public void deleteProjectBySlug(String slug) {
        require(slug != null && !slug.isBlank(), new BadRequestException("Slug is required"));

        String cleanSlug = slugify(slug);

        Project project = projectRepository.findBySlug(cleanSlug)
                .orElseThrow(() -> new BadRequestException("Project not found"));

        ProjectType projectType = projectTypeRepository.findById(project.getTypeId())
                .orElseThrow(() -> new BadRequestException("Project type not found"));

        deleteProject(project, projectType);
    }

    private void deleteProject(Project project, ProjectType projectType) {
        if (project.getTypeId() == null || !project.getTypeId().equals(projectType.getId())) {
            throw new RuntimeException("Cannot delete project");
        }

        storageService.deleteProjectFolder(slugify(projectType.getSlug()), slugify(project.getSlug()));
        projectRepository.deleteById(project.getId());
    }

    private static void require(boolean condition, RuntimeException ex) {
        if (!condition) {
            throw ex;
        }
    }
}