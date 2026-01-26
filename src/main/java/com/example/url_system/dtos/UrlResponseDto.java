package com.example.url_system.dtos;

import java.time.Instant;


/**
 * Dto to response without leaking sensitive data
 *
 * @param url
 * @param createdAt date of creation
 */
public record UrlResponseDto(
        String url,
        Instant createdAt
) {
}
