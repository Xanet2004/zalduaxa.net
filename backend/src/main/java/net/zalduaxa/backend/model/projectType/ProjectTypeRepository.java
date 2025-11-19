package net.zalduaxa.backend.model.projectType;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
    ProjectType findByName(String name);
}
