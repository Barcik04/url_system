package com.example.url_system.Mocks;

import com.example.url_system.jwt.AuthController;
import com.example.url_system.jwt.AuthEntryPointJwt;
import com.example.url_system.jwt.AuthTokenFilter;
import com.example.url_system.jwt.JwtUtils;

import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UrlRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.security.SecurityConfig;
import com.example.url_system.services.UrlService;
import com.example.url_system.utils.config.JacksonConfig;
import com.example.url_system.utils.ratelimit.RateLimitFilter;
import com.example.url_system.utils.ratelimit.RateLimitService;
import com.example.url_system.utils.redis.RedisCacheClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.mockito.ArgumentMatchers.any;


@Import({SecurityConfig.class, RateLimitFilter.class, RateLimitService.class, JacksonConfig.class, RedisMockConfig.class})
@WebMvcTest(controllers = AuthController.class, properties = "ratelimit.enabled=true")
@ActiveProfiles("test")
class AuthTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;


    @MockitoBean
    UrlService urlService;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    UrlRepository urlRepository;

    @MockitoBean
    AuthEntryPointJwt authEntryPointJwt;
    @MockitoBean
    AuthTokenFilter authTokenFilter;
    @MockitoBean
    OutboxEventRepository outboxEventRepository;
    @MockitoBean
    RedisCacheClient redisCacheClient;



    @MockitoBean
    JwtUtils jwtUtils;
    @MockitoBean
    org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Mock
    ValueOperations<String, String> valueOps;




    @BeforeEach
    void allowRequestsThroughFilters() throws Exception {

        doAnswer(inv -> {
            var req = inv.getArgument(0, jakarta.servlet.ServletRequest.class);
            var res = inv.getArgument(1, jakarta.servlet.ServletResponse.class);
            var chain = inv.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(req, res);
            return null;
        }).when(authTokenFilter).doFilter(any(), any(), any());
    }


    @Test
    void should_return_429_when_rate_limit_exceeded() throws Exception {
        String body = """
        { "username": "igor", "password": "12345678" }
        """;

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
        }

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests());
    }
}
