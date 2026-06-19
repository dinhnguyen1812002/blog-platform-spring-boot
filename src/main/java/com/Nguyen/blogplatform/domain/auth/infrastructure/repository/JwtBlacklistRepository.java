package com.Nguyen.blogplatform.domain.auth.infrastructure.repository;



import com.Nguyen.blogplatform.domain.auth.domain.model.JwtBlacklist;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for JWT token blacklist operations.
 */
@Repository
public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, String> {

    /**
     * Check if a token hash exists in the blacklist.
     *
     * @param tokenHash SHA-256 hash of the JWT token
     * @return true if token is blacklisted
     */
    boolean existsByTokenHash(String tokenHash);

    /**
     * Find blacklist entry by token hash.
     *
     * @param tokenHash SHA-256 hash of the JWT token
     * @return Optional containing the blacklist entry if found
     */
    @Query("SELECT jb FROM JwtBlacklist jb WHERE jb.tokenHash = :tokenHash")
    JwtBlacklist findByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Delete all blacklist entries that have expired.
     * This is used by the scheduled cleanup task.
     *
     * @param now Current timestamp
     * @return Number of deleted entries
     */
    @Modifying
    @Query("DELETE FROM JwtBlacklist jb WHERE jb.expiryDate < :now")
    int deleteByExpiryDateBefore(@Param("now") Instant now);

    /**
     * Find all blacklist entries for a specific user.
     * Used for bulk token revocation.
     *
     * @param userId User ID
     * @return List of blacklist entries for the user
     */
    @Query("SELECT jb FROM JwtBlacklist jb WHERE jb.userId = :userId ORDER BY jb.revokedAt DESC")
    List<JwtBlacklist> findAllByUserId(@Param("userId") String userId);

    /**
     * Count total blacklist entries.
     *
     * @return Total number of blacklisted tokens
     */
    long count();

    /**
     * Count expired blacklist entries.
     *
     * @param now Current timestamp
     * @return Number of expired entries
     */
    @Query("SELECT COUNT(jb) FROM JwtBlacklist jb WHERE jb.expiryDate < :now")
    long countExpired(@Param("now") Instant now);
}
