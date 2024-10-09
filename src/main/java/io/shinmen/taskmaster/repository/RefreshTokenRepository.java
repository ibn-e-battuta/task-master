package io.shinmen.taskmaster.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.shinmen.taskmaster.entity.RefreshToken;
import io.shinmen.taskmaster.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
}
