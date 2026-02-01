package com.example.url_system.utils.scheduling;

import com.example.url_system.utils.emailSender.SmtpEmailSender;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ScheduledEmailJob {

    private final SmtpEmailSender emailSender;

    public ScheduledEmailJob(SmtpEmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Scheduled(fixedRateString = "PT1M")
    public void sendDemoEmail() {
        emailSender.send(
                "igor.bb00@gmail.com",
                "Scheduled email test",
                "Hello! This email was sent by a Spring @Scheduled job."
        );
    }
}

