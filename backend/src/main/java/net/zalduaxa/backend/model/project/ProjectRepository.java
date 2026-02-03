package net.zalduaxa.backend.model.project;

import java.util.List;

import org.springframework.data.jpa.repository.*;
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
}
