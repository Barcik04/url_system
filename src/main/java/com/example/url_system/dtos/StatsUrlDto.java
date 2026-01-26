package com.example.url_system.dtos;


import java.time.Instant;


/**
 * dto for displaying stats for url
 *
 * @param longUrl url
 * @param code shortened url
 * @param createdAt time of creation
 * @param expiresAt time of expiry
 * @param clicks number of times the url was clicked
 */
public record StatsUrlDto (
        String longUrl,
        String code,
        Instant createdAt,
        Instant expiresAt,
        Long clicks
){
}
