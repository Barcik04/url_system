package com.example.url_system.utils.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisCacheClient {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisCacheClient(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redis.opsForValue().get(key);
            if (json == null) return Optional.empty();
            return Optional.of(mapper.readValue(json, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redis.opsForValue().set(key, mapper.writeValueAsString(value), ttl);
        } catch (Exception ignored) {}
    }


    public int incrementAndGet(String key, Duration ttl) {
        Long value = redis.opsForValue().increment(key);
        if (value != null && value == 1) {
            redis.expire(key, ttl);
        }
        return value != null ? value.intValue() : 0;
    }

    public boolean exists(String key) {
        Boolean ok = redis.hasKey(key);
        return ok != null && ok;
    }
}
