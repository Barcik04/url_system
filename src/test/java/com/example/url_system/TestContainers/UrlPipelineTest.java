package com.example.url_system.TestContainers;

import org.springframework.http.MediaType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UrlPipelineTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setup() {
        jdbc.execute("TRUNCATE TABLE urls, users RESTART IDENTITY CASCADE");

        User user = new User("igor", passwordEncoder.encode("12345678"));
        userRepository.save(user);
    }


    @Test
    @WithUserDetails(value = "igor", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
