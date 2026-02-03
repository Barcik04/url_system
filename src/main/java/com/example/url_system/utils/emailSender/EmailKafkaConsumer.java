package com.example.url_system.utils.emailSender;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
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
}
