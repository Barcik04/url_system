package com.example.url_system.dtos;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Dto used to patch url
 *
 * @param longUrl url you want to transform
 * @param expiredAt expiry date
 */
public record PatchUrlDto(
        @Size(max = 2048)
        String longUrl,
        @Future(message = "expiry date has to be in the future")
        Instant expiredAt
){
}

