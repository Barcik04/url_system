package com.example.url_system.Mocks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.*;

@TestConfiguration
class RedisMockConfig {

    @Bean
    @Primary
    StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);

        when(template.opsForValue()).thenReturn(ops);
        when(template.hasKey(anyString())).thenReturn(false);

        when(template.execute(any(), anyList(), anyString()))
                .thenReturn(1L, 2L, 3L, 4L); // for AUTH_REGISTER=3

        doNothing().when(ops).set(anyString(), anyString(), any());

        return template;
    }
}
