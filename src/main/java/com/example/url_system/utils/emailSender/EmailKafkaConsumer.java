package com.example.url_system.utils.emailSender;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Profile("stage")
public class EmailKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;

    public EmailKafkaConsumer(ObjectMapper objectMapper, EmailSender emailSender) {
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
    }

    @KafkaListener(
            topics = "email.send.requested",
            groupId = "email-service"
    )
    public void consume(String payloadJson) {
        try {
            EmailPayload p = objectMapper.readValue(payloadJson, EmailPayload.class);
            emailSender.send(p.toEmail(), p.subject(), p.body());
            log.info("url expiration email sent to {}", p.toEmail());
        } catch (Exception ex) {
            log.error("Email consumer failed: {}", ex.toString());
            throw new RuntimeException(ex);
        }
    }

    @KafkaListener(
            topics = "signin.fail",
            groupId = "signin-service"
    )
    public void consumeFail(String payloadJson) {
        try {
            EmailPayload p = objectMapper.readValue(payloadJson, EmailPayload.class);
            emailSender.send(p.toEmail(), p.subject(), p.body());
            log.info("Signin failure email sent to {}", p.toEmail());
        } catch (Exception ex) {
            log.error("Email consumer failed: {}", ex.toString());
            throw new RuntimeException(ex);
        }
    }






    @KafkaListener(topics = "email.verify", groupId = "email-consumers")
    public void consumeVerify(String payloadJson) throws Exception {
        try {
            EmailPayload p = objectMapper.readValue(payloadJson, EmailPayload.class);
            String html = buildHtml(p.body());


            emailSender.send(p.toEmail(), p.subject(), html);
        } catch (Exception ex) {
            log.error("Email verify consumer failed: {}", ex.toString());
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
