package com.example.url_system.services;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import com.example.url_system.dtos.avatar.PresignAvatarUploadResponse;
import com.example.url_system.dtos.avatar.UserAvatarResponse;
import com.example.url_system.models.User;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.utils.config.AvatarsS3Properties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Profile("prod & !stage")
public class AvatarService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final AvatarsS3Properties props;
    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final UserRepository userRepository;

    public AvatarService(
            AvatarsS3Properties props,
            S3Presigner presigner,
            S3Client s3Client,
            UserRepository userRepository
    ) {
        this.props = props;
        this.presigner = presigner;
        this.s3Client = s3Client;
        this.userRepository = userRepository;
    }

    public PresignAvatarUploadResponse presignUpload(String userEmailOrUsername, String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported contentType: " + contentType);
        }

        User user = userRepository.findByUsername(userEmailOrUsername)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String ext = extension(contentType);
        String key = props.keyPrefix() + "/" + user.getId() + "/" + UUID.randomUUID() + ext;

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(props.presignTtlSeconds()))
                .putObjectRequest(putReq)
                .build();

        String uploadUrl = presigner.presignPutObject(presignReq).url().toString();
        return new PresignAvatarUploadResponse(key, uploadUrl, props.presignTtlSeconds());
    }

    @Transactional
    public UserAvatarResponse confirmUpload(String userEmailOrUsername, String key) {
        User user = userRepository.findByUsername(userEmailOrUsername)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String requiredPrefix = props.keyPrefix() + "/" + user.getId() + "/";
        if (key == null || !key.startsWith(requiredPrefix)) {
            throw new IllegalArgumentException("Invalid key for this user.");
        }

        String oldKey = user.getAvatarKey();
        user.setAvatarKey(key);
        userRepository.save(user);

        if (oldKey != null && !oldKey.isBlank() && !oldKey.equals(key)) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(oldKey)
                    .build());
        }

        return new UserAvatarResponse(user.getAvatarKey());
    }

    private String extension(String contentType) {
        if ("image/jpeg".equals(contentType)) return ".jpg";
        if ("image/png".equals(contentType)) return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        throw new IllegalArgumentException("Unsupported contentType: " + contentType);
    }
}
