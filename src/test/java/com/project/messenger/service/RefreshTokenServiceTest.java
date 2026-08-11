package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectUnauthorizedException;
import com.project.messenger.model.RefreshToken;
import com.project.messenger.model.User;
import com.project.messenger.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    private static final long SEVEN_DAYS = 604800000L;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);

        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenExpiration",
                SEVEN_DAYS
        );
    }

    @Test
    void issueCreatesAndStoresHashedRefreshToken() {
        User user = new User();

        Instant earliestExpiration = Instant.now().plusMillis(SEVEN_DAYS);

        String rawToken = refreshTokenService.issue(user);

        Instant latestExpiration = Instant.now().plusMillis(SEVEN_DAYS);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertNotNull(rawToken);
        assertEquals(43, rawToken.length());

        assertSame(user, savedToken.getUser());
        assertNotEquals(rawToken, savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());
        assertNull(savedToken.getRevokedAt());

        assertFalse(savedToken.getExpiresAt().isBefore(earliestExpiration));
        assertFalse(savedToken.getExpiresAt().isAfter(latestExpiration));
    }

    @Test
    void consumeReturnsUserAndRevokesValidToken() {
        User user = new User();

        RefreshToken storedToken = new RefreshToken(
                user,
                "stored-hash",
                Instant.now().plusSeconds(60)
        );

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(storedToken));

        User result = refreshTokenService.consume("raw-refresh-token");

        assertSame(user, result);
        assertNotNull(storedToken.getRevokedAt());
    }

    @Test
    void consumeRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(
                AppObjectUnauthorizedException.class,
                () -> refreshTokenService.consume("unknown-refresh-token")
        );
    }
    @Test
    void consumeRejectsExpiredToken() {
        User user = new User();

        RefreshToken expiredToken = new RefreshToken(
                user,
                "stored-hash",
                Instant.now().minusSeconds(1)
        );

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                AppObjectUnauthorizedException.class,
                () -> refreshTokenService.consume("expired-refresh-token")
        );

        assertNull(expiredToken.getRevokedAt());
    }
    @Test
    void consumeRejectsRevokedToken() {
        User user = new User();

        RefreshToken revokedToken = new RefreshToken(
                user,
                "stored-hash",
                Instant.now().plusSeconds(60)
        );

        revokedToken.revoke(Instant.now());

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(revokedToken));

        assertThrows(
                AppObjectUnauthorizedException.class,
                () -> refreshTokenService.consume("revoked-refresh-token")
        );
    }
}
