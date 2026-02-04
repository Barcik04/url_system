package com.example.url_system.jwt;


import com.example.url_system.models.RefreshToken;
import com.example.url_system.models.User;
import com.example.url_system.repositories.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCodec codec;

    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, RefreshTokenCodec codec) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.codec = codec;
    }

    @Transactional
    public String issueRefreshToken(User user) {
        String raw = codec.generateRawToken();
        String hash = codec.sha256(raw);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plus(REFRESH_TTL));
        token.setRevokedAt(null);

        refreshTokenRepository.save(token);
        return raw;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidByRawToken(String rawToken) {
        String hash = codec.sha256(rawToken);

        return refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    public void revokeByRawToken(String rawToken) {
        String hash = codec.sha256(rawToken);

        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                .ifPresent(rt -> {
                    rt.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(rt);
                });
    }


    @Transactional
    public void deleteAllExpiredOrRevokedRefreshTokens() {
        Instant now = Instant.now();
        int count = refreshTokenRepository.deleteAllExpired(now);
        log.info("Deleted {} refresh tokens", count);

    }
}

