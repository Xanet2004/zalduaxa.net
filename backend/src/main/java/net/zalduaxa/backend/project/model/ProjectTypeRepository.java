package net.zalduaxa.backend.project.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, Integer> {
    Optional<ProjectType> findByName(String name);

    Optional<ProjectType> findBySlug(String slug);
}