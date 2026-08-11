package com.project.messenger.service;

import com.project.messenger.core.exception.AppObjectUnauthorizedException;
import com.project.messenger.model.RefreshToken;
import com.project.messenger.model.User;
import com.project.messenger.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${auth.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private String generateRawToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] digest = messageDigest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public String issue(User user) {
        String rawToken = generateRawToken();

        RefreshToken refreshToken = new RefreshToken(
                user,
                hash(rawToken),
                Instant.now().plusMillis(refreshTokenExpiration)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public User consume(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new AppObjectUnauthorizedException("REFRESH_TOKEN", "Refresh token is invalid or expired"));

        Instant now = Instant.now();

        if (!refreshToken.isUsableAt(now)) {
            throw new AppObjectUnauthorizedException("REFRESH_TOKEN", "Refresh token is invalid or expired");
        }

        refreshToken.revoke(now);

        return refreshToken.getUser();
    }
}
