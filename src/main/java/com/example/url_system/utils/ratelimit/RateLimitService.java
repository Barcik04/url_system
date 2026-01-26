package com.example.url_system.utils.ratelimit;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .maximumSize(100_000)
            .build();

    private Bucket newBucket(RateLimitPolicy policy) {
        return Bucket.builder()
                .addLimit(policy.getBandwidth())
                .build();
    }

    public boolean tryConsume(String key, RateLimitPolicy policy) {
        String cacheKey = policy.name() + "|" + key; // separate bucket per policy per user/ip
        Bucket bucket = buckets.get(cacheKey, k -> newBucket(policy));
        return bucket.tryConsume(1);
    }
}
