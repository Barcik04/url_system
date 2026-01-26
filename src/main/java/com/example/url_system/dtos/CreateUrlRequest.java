package com.example.url_system.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;


/**
 * Dto used to create url
 *
 * @param longUrl url you want to transform
 * @param expiredAt expiry date
 */
public record CreateUrlRequest(
        @NotBlank(message = "url cant be blank")
        @Size(max = 2048)
        String longUrl,
        Instant expiredAt
){
}
