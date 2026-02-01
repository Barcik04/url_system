package com.example.url_system;

import com.example.url_system.utils.emailSender.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class UrlSystemApplicationTests {
    @MockitoBean
    private EmailSender emailSender;

    @Test
    void contextLoads() {
    }

}
