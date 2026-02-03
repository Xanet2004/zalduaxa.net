package net.zalduaxa.backend.model.project;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    @Query("""
        SELECT p
        FROM Project p
        JOIN ProjectType pt ON pt.id = p.typeId
        WHERE pt.storagePath = :slug
    """)
    List<Project> findByProjectTypeStoragePath(@Param("slug") String slug);
}