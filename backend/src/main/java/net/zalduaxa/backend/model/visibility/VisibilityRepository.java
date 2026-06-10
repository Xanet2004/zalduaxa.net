package net.zalduaxa.backend.model.visibility;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisibilityRepository extends JpaRepository<Visibility, Integer> {
    Visibility findByName(String name);
}
