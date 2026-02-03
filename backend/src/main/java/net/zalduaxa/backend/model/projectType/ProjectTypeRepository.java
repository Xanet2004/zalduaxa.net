package net.zalduaxa.backend.model.projectType;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
    ProjectType findByName(String name);
    Optional<ProjectType> findBySlug(String slug);
}
