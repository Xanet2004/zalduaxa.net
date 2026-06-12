package net.zalduaxa.backend.auth.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Integer> {

    Optional<Session> findByUserId(Integer userId);

    Optional<Session> findByToken(String token);

    void deleteByUserId(Integer userId);
}