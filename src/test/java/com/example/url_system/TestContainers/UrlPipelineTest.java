package com.example.url_system.TestContainers;

import com.example.url_system.controllers.AvatarControllerV1;
import com.example.url_system.services.AvatarService;
import com.example.url_system.utils.emailSender.EmailSender;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.url_system.models.User;
import com.example.url_system.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class UrlPipelineTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    private EmailSender emailSender;
    @MockitoBean
    AvatarControllerV1 avatarControllerV1;
    @MockitoBean
    AvatarService avatarService;


    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static {
        redis.start();
    }

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    void setup() {
        jdbc.execute("TRUNCATE TABLE urls, users, idempotency_keys, outbox_events RESTART IDENTITY CASCADE");

        User user = new User("igor.bb00@gmail.com", passwordEncoder.encode("12345678"));
        userRepository.save(user);
    }


    @Test
    @WithUserDetails(value = "igor.bb00@gmail.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void shouldCreateUrl_endToEnd() throws Exception {
        String json = """
                {
                "longUrl":"https://google.com"
                 }
                """;

        mockMvc.perform(post("/api/v1/urls")
                        .header("Idempotency-Key", "wodawk")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());


    }
}
