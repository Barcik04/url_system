package com.example.url_system.dtos;

import java.time.Instant;

public record OutboxPayloadDto(
        long urlId,
        long userId,
        String email,
        String code,
        Instant expiredAt
) {}