package com.example.url_system.utils.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3.avatars")
public record AvatarsS3Properties(
        String bucket,
        String region,
        String keyPrefix,
        long presignTtlSeconds
) {}
