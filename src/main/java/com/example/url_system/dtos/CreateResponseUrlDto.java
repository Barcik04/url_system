package com.example.url_system.dtos;

import java.time.Instant;

/**
 * Dto to display url after creation
 *
 * @param shortUrl shortened url code
 * @param createdAt time of creation
 * @param expiresAt time of expiry
 */
public record CreateResponseUrlDto(
        String shortUrl,
        Instant createdAt,
        Instant expiresAt
) {
}
