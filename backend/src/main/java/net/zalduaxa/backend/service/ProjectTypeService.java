package net.zalduaxa.backend.service;

import static net.zalduaxa.backend.utils.SlugUtils.slugify;

import java.util.List;

import org.springframework.stereotype.Service;

import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.model.project.Project;
import net.zalduaxa.backend.model.project.ProjectRepository;
import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;
import net.zalduaxa.backend.model.requestProjectType.RequestProjectType;
import net.zalduaxa.backend.model.responseProjectType.ResponseProjectType;

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

    public List<ResponseProjectType> getAllProjectTypes() {
        return projectTypeRepository.findAll()
                .stream()
                .map(ResponseProjectType::new)
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

        storageService.saveProjectTypeImage(cleanSlug, request.getImage());

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