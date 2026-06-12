package net.zalduaxa.backend.project.service;

import static net.zalduaxa.backend.common.util.SlugUtils.slugify;

import java.util.List;

import org.springframework.stereotype.Service;

import net.zalduaxa.backend.project.dto.request.ProjectTypeRequest;
import net.zalduaxa.backend.project.dto.response.ProjectTypeResponse;
import net.zalduaxa.backend.common.exception.BadRequestException;
import net.zalduaxa.backend.project.model.Project;
import net.zalduaxa.backend.project.model.ProjectRepository;
import net.zalduaxa.backend.project.model.ProjectType;
import net.zalduaxa.backend.project.model.ProjectTypeRepository;
import net.zalduaxa.backend.storage.service.StorageService;

@Service
public class ProjectTypeService {

    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectRepository projectRepository;
    private final StorageService storageService;

    public ProjectTypeService(
            ProjectTypeRepository projectTypeRepository,
            ProjectRepository projectRepository,
            StorageService storageService) {
        this.projectTypeRepository = projectTypeRepository;
        this.projectRepository = projectRepository;
        this.storageService = storageService;
    }

    public List<ProjectTypeResponse> getAllProjectTypes() {
        return projectTypeRepository.findAll()
                .stream()
                .map(ProjectTypeResponse::new)
                .toList();
    }

    public void createProjectType(ProjectTypeRequest request) {
        require(request != null, new BadRequestException("Request body is required"));
        require(request.getName() != null && !request.getName().isBlank(),
                new BadRequestException("Name is required"));

        require(projectTypeRepository.findByName(request.getName()).isEmpty(),
                new BadRequestException("Project Type already exists"));

        String cleanSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? slugify(request.getSlug())
                : slugify(request.getName());

        storageService.saveProjectTypeImage(cleanSlug, request.getImage());

        ProjectType projectType = new ProjectType(
                request.getName(),
                request.getDescription(),
                cleanSlug);

        projectTypeRepository.save(projectType);
    }

    public void deleteProjectType(String name) {
        require(name != null && !name.isBlank(), new BadRequestException("Name is required"));

        ProjectType projectType = projectTypeRepository.findByName(name)
                .orElseThrow(() -> new BadRequestException("Project type not found"));

        deleteProjectType(projectType);
    }

    public void deleteProjectTypeBySlug(String slug) {
        require(slug != null && !slug.isBlank(), new BadRequestException("Slug is required"));

        String cleanSlug = slugify(slug);

        ProjectType projectType = projectTypeRepository.findBySlug(cleanSlug)
                .orElseThrow(() -> new BadRequestException("Project type not found"));

        deleteProjectType(projectType);
    }

    private void deleteProjectType(ProjectType projectType) {
        for (Project project : projectRepository.findByTypeId(projectType.getId())) {
            deleteProject(project, projectType);
        }

        storageService.deleteProjectTypeFolder(projectType.getSlug());
        projectTypeRepository.deleteById(projectType.getId());
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