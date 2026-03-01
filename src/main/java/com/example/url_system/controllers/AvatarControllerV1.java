package com.example.url_system.controllers;

import com.example.url_system.dtos.avatar.ConfirmAvatarUploadRequest;
import com.example.url_system.dtos.avatar.PresignAvatarUploadRequest;
import com.example.url_system.dtos.avatar.PresignAvatarUploadResponse;
import com.example.url_system.dtos.avatar.UserAvatarResponse;
import com.example.url_system.services.AvatarService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile("prod & !stage")
@RequestMapping("/api/v1/users/me/avatar")
public class AvatarControllerV1 {

    private final AvatarService avatarService;

    public AvatarControllerV1(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping("/presign")
    public ResponseEntity<PresignAvatarUploadResponse> presign(
            @Valid @RequestBody PresignAvatarUploadRequest req,
            Authentication authentication
    ) {
        String name = authentication.getName();
        return ResponseEntity.ok(avatarService.presignUpload(name, req.contentType()));
    }

    @PutMapping
    public ResponseEntity<UserAvatarResponse> confirm(
            @Valid @RequestBody ConfirmAvatarUploadRequest req,
            Authentication authentication
    ) {
        String name = authentication.getName();
        return ResponseEntity.ok(avatarService.confirmUpload(name, req.key()));
    }
}

