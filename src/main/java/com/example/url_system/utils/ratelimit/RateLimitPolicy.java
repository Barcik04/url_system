package com.example.url_system.utils.ratelimit;

import java.time.Duration;

public enum RateLimitPolicy {

    AUTH_LOGIN(5, Duration.ofMinutes(1), Duration.ofMinutes(2)),
    AUTH_REGISTER(3, Duration.ofMinutes(1), Duration.ofMinutes(5)),
    REGULAR(40, Duration.ofSeconds(10), null);

    private final int limit;
    private final Duration window;
    private final Duration ban;

    RateLimitPolicy(int limit, Duration window, Duration ban) {
        this.limit = limit;
        this.window = window;
        this.ban = ban;
    }

    public int getLimit() {
        return limit;
    }

    public Duration getWindow() {
        return window;
    }

    public Duration getBan() {
        return ban;
    }
}
