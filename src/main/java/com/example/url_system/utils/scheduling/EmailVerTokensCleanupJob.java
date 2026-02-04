package com.example.url_system.utils.scheduling;

import com.example.url_system.services.EmailVerificationService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmailVerTokensCleanupJob {

    private final EmailVerificationService emailVerificationService;

    public EmailVerTokensCleanupJob(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @Retry(name = "baseService")
    @Scheduled(cron = "0 4 3 * * *", zone = "Europe/Warsaw")
    @Transactional
    public void run() {
        emailVerificationService.deleteExpiredTokens();
    }
}
