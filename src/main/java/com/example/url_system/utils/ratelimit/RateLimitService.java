package com.example.url_system.utils.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RateLimitService {

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> incrWithExpire;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;

        this.incrWithExpire = new DefaultRedisScript<>();
        this.incrWithExpire.setResultType(Long.class);
        this.incrWithExpire.setScriptText("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
              redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return current
        """);
    }

    public boolean tryConsume(String subjectKey, RateLimitPolicy policy) {
        String counterKey = "rl:" + policy.name() + ":" + subjectKey;

        Duration ban = policy.getBan();
        String banKey = (ban != null) ? ("rlban:" + policy.name() + ":" + subjectKey) : null;

        if (banKey != null && Boolean.TRUE.equals(redis.hasKey(banKey))) {
            return false;
        }

        Long count = redis.execute(
                incrWithExpire,
                List.of(counterKey),
                String.valueOf(policy.getWindow().toSeconds())
        );

        long current = (count != null) ? count : Long.MAX_VALUE;

        if (current > policy.getLimit()) {
            if (banKey != null) {
                redis.opsForValue().set(banKey, "1", ban);
            }
            return false;
        }

        return true;
    }
}
