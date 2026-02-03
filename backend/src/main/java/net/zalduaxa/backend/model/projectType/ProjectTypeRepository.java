package net.zalduaxa.backend.model.projectType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.zalduaxa.backend.model.project.Project;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
    ProjectType findByName(String name);
    List<Project> findByTypeId(Integer typeId);
}
