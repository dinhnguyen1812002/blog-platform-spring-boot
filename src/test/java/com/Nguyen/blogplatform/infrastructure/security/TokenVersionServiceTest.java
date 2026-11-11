package com.Nguyen.blogplatform.infrastructure.security;

import glide.api.GlideClient;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TokenVersionServiceTest {

    private final GlideClient glide = mock(GlideClient.class);
    private final TokenVersionService service = new TokenVersionService(glide);

    @Test
    void neverRevokedUserIsVersionZero() {
        when(glide.get("auth:token-version:u1")).thenReturn(CompletableFuture.completedFuture(null));

        assertThat(service.currentVersion("u1")).isZero();
        assertThat(service.isCurrent("u1", 0)).isTrue();
    }

    @Test
    void tokensIssuedBeforeRevocationAreRejected() {
        when(glide.get("auth:token-version:u1")).thenReturn(CompletableFuture.completedFuture("3"));

        assertThat(service.isCurrent("u1", 2)).isFalse();
        assertThat(service.isCurrent("u1", 3)).isTrue();
    }

    @Test
    void failsOpenWhenValkeyIsDown() {
        when(glide.get("auth:token-version:u1"))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("connection refused")));

        assertThat(service.isCurrent("u1", 0)).isTrue();
    }

    @Test
    void revocationFailureIsNotSilent() {
        when(glide.incr("auth:token-version:u1"))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("connection refused")));

        assertThatThrownBy(() -> service.revokeAll("u1")).isInstanceOf(IllegalStateException.class);
    }
}
