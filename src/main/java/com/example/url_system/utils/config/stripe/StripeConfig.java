package com.example.url_system.utils.config.stripe;


import com.stripe.StripeClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripeConfig {

    @Bean
    public StripeClient stripeClient(StripeProperties stripeProperties) {
        return StripeClient.builder()
                .setApiKey(stripeProperties.getSecretKey())
                .build();
    }
}