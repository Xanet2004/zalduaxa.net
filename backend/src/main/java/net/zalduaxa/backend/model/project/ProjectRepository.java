package net.zalduaxa.backend.model.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Integer> {

    @Query(
      value = """
        SELECT p.*
        FROM zalduaxanet.project p
        JOIN zalduaxanet.project_type pt ON pt.id = p.type_id
        WHERE pt.slug = :slug
      """,
      nativeQuery = true
    )
    List<Project> findByProjectTypeSlug(@Param("slug") String slug);
    Optional<Project> findBySlug(String slug);
    Optional<Project> findByName(String name);
    boolean existsBySlug(String slug);
}
