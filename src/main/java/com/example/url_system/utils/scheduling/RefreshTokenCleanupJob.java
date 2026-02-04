package com.example.url_system.utils.scheduling;


import com.example.url_system.jwt.RefreshTokenService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefreshTokenCleanupJob {
    private final RefreshTokenService  refreshTokenService;

    public RefreshTokenCleanupJob(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }


    @Retry(name = "baseService")
    @Scheduled(cron = "0 3 3 * * *", zone = "Europe/Warsaw")
    @Transactional
    public void run() {
        refreshTokenService.deleteAllExpiredOrRevokedRefreshTokens();
    }
}
