package com.example.url_system.dtos.avatar;

import jakarta.validation.constraints.NotBlank;

public record PresignAvatarUploadRequest(@NotBlank String contentType
) {
}
