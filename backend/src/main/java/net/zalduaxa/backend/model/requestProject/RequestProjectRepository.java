package net.zalduaxa.backend.model.requestProject;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequestProjectRepository extends JpaRepository<RequestProject, Integer> {

    @Query(
      value = """
        SELECT p.*
        FROM zalduaxanet.project p
        JOIN zalduaxanet.project_type pt ON pt.id = p.type_id
        WHERE pt.slug = :slug
      """,
      nativeQuery = true
    )
    List<RequestProject> findByProjectTypeSlug(@Param("slug") String slug);
    Optional<RequestProject> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
