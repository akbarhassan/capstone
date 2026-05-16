package com.ga.capstone.repositories;


import com.ga.capstone.models.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUserId(Long userId);

    List<EmailVerificationToken> findByExpiresAtBefore(LocalDateTime now);

    @Query("SELECT t FROM EmailVerificationToken t WHERE t.token = :token AND t.expiresAt > :now AND t.used = false")
    Optional<EmailVerificationToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Atomically mark a token as used. Returns 1 if successful, 0 if already used (race lost).
     */
    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.used = true WHERE t.token = :token AND t.used = false")
    int markAsUsedAtomically(@Param("token") String token);

    void deleteByUserId(Long userId);

}
