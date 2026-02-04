package com.example.url_system.repositories;

import com.example.url_system.models.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
    void deleteByUserId(Long userId);


    @Modifying
    @Query("""
    DELETE FROM EmailVerificationToken e
    WHERE e.expiresAt < :now
    """)
    int deleteExpiredTokens(@Param("now") Instant now);
}
