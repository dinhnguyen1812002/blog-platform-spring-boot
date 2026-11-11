package com.Nguyen.blogplatform.infrastructure.security;

import glide.api.GlideClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Per-user token version stored in Valkey ({@code auth:token-version:{userId}}).
 *
 * Every access token carries the version that was current when it was issued (claim "tv").
 * {@link #revokeAll(String)} bumps the version, which instantly invalidates every access
 * token issued before — used on password change/reset and ban.
 *
 * Availability: if Valkey is unreachable, checks fail open (logged). Ban is still enforced
 * independently because AuthTokenFilter loads the user and checks isAccountNonLocked().
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenVersionService {

    public static final String CLAIM = "tv";
    private static final String KEY_PREFIX = "auth:token-version:";

    private final GlideClient glideClient;

    /** Current version for the user (0 when never revoked). */
    public long currentVersion(String userId) {
        try {
            String value = glideClient.get(KEY_PREFIX + userId).get();
            return value == null ? 0L : Long.parseLong(value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted reading token version for {}", userId);
            return -1L;
        } catch (Exception e) {
            log.error("Could not read token version for {} from Valkey", userId, e);
            return -1L; // unknown
        }
    }

    /** Invalidate every access token issued so far for this user. */
    public void revokeAll(String userId) {
        try {
            glideClient.incr(KEY_PREFIX + userId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while revoking tokens", e);
        } catch (Exception e) {
            // Revocation must not silently fail: surface it to the caller
            throw new IllegalStateException("Could not revoke tokens for user " + userId, e);
        }
    }

    /**
     * True if a token carrying {@code tokenVersion} is still valid.
     * Unknown current version (Valkey down) → fail open.
     */
    public boolean isCurrent(String userId, long tokenVersion) {
        long current = currentVersion(userId);
        return current < 0 || tokenVersion == current;
    }
}
