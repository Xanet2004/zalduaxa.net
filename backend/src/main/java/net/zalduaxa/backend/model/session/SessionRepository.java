package net.zalduaxa.backend.model.session;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<Session> findByUserId(Long userId);
}
