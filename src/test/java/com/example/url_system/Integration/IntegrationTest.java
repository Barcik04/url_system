package com.example.url_system.Integration;

import com.example.url_system.dtos.CreateResponseUrlDto;
import com.example.url_system.dtos.CreateUrlRequest;
import com.example.url_system.dtos.StatsUrlDto;
import com.example.url_system.exceptions.UrlExpiredException;
import com.example.url_system.models.Role;
import com.example.url_system.models.Url;
import com.example.url_system.models.User;
import com.example.url_system.repositories.IdempotencyKeyRepository;
import com.example.url_system.repositories.UrlRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.IdempotencyKeyService;
import com.example.url_system.services.UrlService;
import com.example.url_system.utils.emailSender.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;


import java.time.*;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class IntegrationTest {
    @Autowired private UrlRepository urlRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired private UrlService urlService;
    @Autowired private IdempotencyKeyService idempotencyKeyService;

    @Autowired
    private JdbcTemplate jdbc;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private EmailSender emailSender;


    @BeforeEach
    void cleanDb() {
        jdbc.execute("""
        TRUNCATE TABLE
            users,
            urls,
            idempotency_keys
        RESTART IDENTITY CASCADE
    """);
    }


    @Test
    @Transactional
    void createLinkSuccessfully() {
        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//:awdjkaiwawdawkod", null);

        CreateResponseUrlDto createResponseUrlDto = urlService.create(createUrlRequest, null, "lofrk");

        assertEquals("https//:awdjkaiwawdawkod", createResponseUrlDto.url());
    }


    @Test
    void shouldThrow403_whenUrlIsExpired() {
        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//:awdjkaiwawkod", Instant.now());

        UrlExpiredException ex = assertThrows(
                UrlExpiredException.class,
                () -> urlService.create(createUrlRequest, null, "bbgbgbg")
        );

        assertEquals("Url expired", ex.getMessage());
    }


    @Test
    @Transactional
    void shouldGetUrlAndRegisterClickSuccessfully() {
        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//12312312waawd", null);

        CreateResponseUrlDto createResponseUrlDto = urlService.create(createUrlRequest, null, "lldoad");

        Url url = urlService.getUrlAndRegisterClick(createResponseUrlDto.shortUrl());

        assertEquals(1, url.getClicks());

        urlService.getUrlAndRegisterClick(createResponseUrlDto.shortUrl());

        assertEquals(2, url.getClicks());
        assertEquals(url.getLongUrl(), createUrlRequest.longUrl());
    }



    @Test
    @Transactional
    void shouldGetStatsSuccessfully() {
        User user = new User("igor.bb00@gmail.com", "12345678", Role.USER);
        userRepository.save(user);

        User foundUser = userRepository.findByUsername("igor.bb00@gmail.com").orElseThrow();

        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//12312312waawd", null);

        CreateResponseUrlDto createResponseUrlDto = urlService.create(createUrlRequest, foundUser.getId(), "kkfke");

        StatsUrlDto statsUrlDto = urlService.getStatsUrl(createResponseUrlDto.shortUrl(), foundUser.getId());

        assertEquals(0, statsUrlDto.clicks());
        assertEquals(statsUrlDto.code(), createResponseUrlDto.shortUrl());
    }


    @Test
    @Transactional
    void shouldThrow404_whenUrlIsNotFound() {
        User user = new User("igor.bb010@gmail.com", "12345678", Role.USER);
        userRepository.save(user);

        User foundUser = userRepository.findByUsername("igor.bb010@gmail.com").orElseThrow();

        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//12312312waawd", null);

        CreateResponseUrlDto createResponseUrlDto = urlService.create(createUrlRequest, null, "awd222");

        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> urlService.getStatsUrl(createResponseUrlDto.shortUrl(), foundUser.getId())
        );

        assertEquals("Url not found", ex.getMessage());
    }



    @Test
    @Transactional
    void shouldReturnPagedLinksForUserSuccessfully() {
        User user = new User("igor.b4b00@gmail.com", "12345678", Role.USER);
        userRepository.save(user);

        Pageable pageable = PageRequest.of(0, 5);

        User foundUser = userRepository.findByUsername("igor.b4b00@gmail.com").orElseThrow();


        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//12312312waawd", null);

        urlService.create(createUrlRequest, foundUser.getId(), "qwe");
        urlService.create(createUrlRequest, foundUser.getId(), "awd");
        urlService.create(createUrlRequest, foundUser.getId(), "ddd");
        urlService.create(createUrlRequest, foundUser.getId(), "aw1d");
        urlService.create(createUrlRequest, foundUser.getId(), "wda");
        urlService.create(createUrlRequest, foundUser.getId(), "dkkkkw");

        Page<StatsUrlDto> urls = urlService.showMyLinks(pageable, foundUser.getId());

        assertEquals(5, urls.getTotalElements());
        assertTrue(urls.stream().anyMatch(a -> a.longUrl().equals(createUrlRequest.longUrl())));
    }


    @Test
    void shouldReturnCachedValue_whenDisplayingStats() {
        User user = new User("igor.b4b00@gmail.com", "12345678", Role.USER);
        userRepository.save(user);

        User foundUser = userRepository.findByUsername("igor.b4b00@gmail.com").orElseThrow();

        CreateUrlRequest createUrlRequest = new CreateUrlRequest("https//12312312waawd", null);

        CreateResponseUrlDto url = urlService.create(createUrlRequest, foundUser.getId(), "123");

        StatsUrlDto statsUrlDto = urlService.getStatsUrl(url.shortUrl(),  foundUser.getId());

        Url repoUrl = urlRepository.findByCode(statsUrlDto.code()).orElseThrow();

        repoUrl.setLongUrl("123lololololol");
        urlRepository.save(repoUrl);

        StatsUrlDto redisUrl = urlService.getStatsUrl(url.shortUrl(), foundUser.getId());

        assertEquals(statsUrlDto.code(), redisUrl.code());
    }

}
