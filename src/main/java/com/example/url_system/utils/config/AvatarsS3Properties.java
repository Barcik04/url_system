package com.example.url_system.utils.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("prod & !stage")
@ConfigurationProperties(prefix = "app.s3.avatars")
public record AvatarsS3Properties(
        String bucket,
        String region,
        String keyPrefix,
        long presignTtlSeconds
) {}
