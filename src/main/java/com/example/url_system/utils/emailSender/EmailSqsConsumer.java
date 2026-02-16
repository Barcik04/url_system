package com.example.url_system.utils.emailSender;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
public class EmailSqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailSqsConsumer.class);

    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;

    public EmailSqsConsumer(ObjectMapper objectMapper, EmailSender emailSender) {
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
    }

    @SqsListener("${app.sqs.emailQueue}")
    public void receive(String body) {
        try {
            EmailPayload p = objectMapper.readValue(body, EmailPayload.class);

            String content = p.body();
            if (p.subject() != null && p.subject().toLowerCase().contains("verify")) {
                content = buildHtml(p.body());
            }

            emailSender.send(p.toEmail(), p.subject(), content);
            log.info("SQS email sent to {}", p.toEmail());
        } catch (Exception ex) {
            log.error("SQS email processing failed, messageBody={}", body, ex);

            throw new RuntimeException(ex);
        }
    }

    private String buildHtml(String verifyUrl) {
        return """
            <div style="font-family: Arial, sans-serif; line-height: 1.5;">
              <h2>Verify your email</h2>
              <p>Click the button below to verify your email address.</p>
              <p>
                <a href="%s"
                   style="display:inline-block;padding:12px 18px;text-decoration:none;border-radius:8px;">
                   Verify email
                </a>
              </p>
              <p>If the button doesn't work, paste this link into your browser:</p>
              <p><a href="%s">%s</a></p>
            </div>
            """.formatted(verifyUrl, verifyUrl, verifyUrl);
    }
}
