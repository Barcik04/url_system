package com.example.url_system.utils.ratelimit;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;

import java.time.Duration;

public enum RateLimitPolicy {

    AUTH_LOGIN(
            Bandwidth.classic(5,
                    Refill.intervally(5, Duration.ofMinutes(1)))
    ),

    AUTH_REGISTER(
            Bandwidth.classic(3,
                    Refill.intervally(3, Duration.ofMinutes(1)))
    ),

    REGULAR(
            Bandwidth.classic(4,
                    Refill.intervally(4, Duration.ofSeconds(10)))
    );

    private final Bandwidth bandwidth;

    RateLimitPolicy(Bandwidth bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Bandwidth getBandwidth() {
        return bandwidth;
    }
}

