package net.zalduaxa.backend.model.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
  List<Project> findByTypeId(Integer typeId);

  Optional<Project> findBySlug(String slug);

  Optional<Project> findByName(String name);

  boolean existsBySlug(String slug);
}