package com.example.url_system.dtos;

public record OutboxPayloadDto(
        String toEmail,
        String subject,
        String body
) {}