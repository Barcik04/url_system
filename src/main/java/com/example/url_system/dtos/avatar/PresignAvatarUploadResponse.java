package com.example.url_system.dtos.avatar;

public record PresignAvatarUploadResponse(
        String key,
        String uploadUrl,
        long expiresInSeconds
) {}

