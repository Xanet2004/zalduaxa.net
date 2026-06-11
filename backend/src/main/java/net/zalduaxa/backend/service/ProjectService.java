package net.zalduaxa.backend.service;

import static net.zalduaxa.backend.utils.SlugUtils.slugify;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.zalduaxa.backend.exception.BadRequestException;
import net.zalduaxa.backend.exception.UnauthorizedException;
import net.zalduaxa.backend.model.project.Project;
import net.zalduaxa.backend.model.project.ProjectRepository;
import net.zalduaxa.backend.model.projectType.ProjectType;
import net.zalduaxa.backend.model.projectType.ProjectTypeRepository;
import net.zalduaxa.backend.dto.response.ProjectResponse;
import net.zalduaxa.backend.model.user.User;

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