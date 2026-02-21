package com.example.url_system.utils.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Profile("prod & !stage")
@Configuration
@EnableConfigurationProperties(AvatarsS3Properties.class)
public class AvatarsS3Config {

    @Bean
    public S3Presigner s3Presigner(AvatarsS3Properties props) {
        return S3Presigner.builder()
                .region(Region.of(props.region()))
                .build();
    }

    @Bean
    public S3Client s3Client(AvatarsS3Properties props) {
        return S3Client.builder()
                .region(Region.of(props.region()))
                .build();
    }
}

