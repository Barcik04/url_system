package com.example.url_system.services;

import com.example.url_system.dtos.*;
import com.example.url_system.exceptions.UrlExpiredException;
import com.example.url_system.models.Url;
import com.example.url_system.models.User;
import com.example.url_system.repositories.UrlRepository;
import com.example.url_system.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class UrlService {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 8;

    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;
    private final UserRepository userRepository;

    private final SecureRandom random = new SecureRandom();

    public UrlService(UrlRepository urlRepository, UrlMapper urlMapper, UserRepository userRepository) {
        this.urlRepository = urlRepository;
        this.urlMapper = urlMapper;
        this.userRepository = userRepository;
    }


    /**
     * Method for creating a shortened link and adding it to users account (optional)
     *
     * @param createUrlRequest {@link CreateUrlRequest} dto to create link
     * @param userId id of an authenticated user
     * @return {@link CreateResponseUrlDto} dto with created url data
     */
    @Transactional
    public CreateResponseUrlDto create(CreateUrlRequest createUrlRequest, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            String code = generateUniqueCode();

            if (createUrlRequest.expiredAt() != null && createUrlRequest.expiredAt().isBefore(Instant.now())) {
                throw new UrlExpiredException("Url expired");
            }

            Url url = new Url(code, createUrlRequest.longUrl(), createUrlRequest.expiredAt());
            user.addUrl(url);
            url.setUser(user);

            return urlMapper.urlToCreateDto(urlRepository.save(url));
        }

        String code = generateUniqueCode();

        if (createUrlRequest.expiredAt() != null && createUrlRequest.expiredAt().isBefore(Instant.now())) {
            throw new UrlExpiredException("Url expired");
        }

        Url url = new Url(code, createUrlRequest.longUrl(), createUrlRequest.expiredAt());

        return urlMapper.urlToCreateDto(urlRepository.save(url));
    }


    /**
     * Method for generating unique code (shortened url) for url
     *
     * @return String of generated shortened url
     */
    private String generateUniqueCode() {
        String uniqueCode = generateCode();

        while (urlRepository.existsByCode(uniqueCode)) {
            uniqueCode = generateCode();
        }

        return uniqueCode;
    }


    /**
     * Generating a random short url
     *
     * @return generated short url
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }

        return sb.toString();
    }


    /**
     * Method for retrieving a link by "clicking" at it
     *
     * @param code shortened link we want to look for
     * @return Retrieved url
     */
    @Transactional
    public Url getUrlAndRegisterClick(String code) {
        Url url = urlRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("link not found"));

        if (url.isExpired(Instant.now())) {
            throw new UrlExpiredException("Url expired");
        }

        url.registerClick();
        urlRepository.save(url);

        return url;
    }



    /**
     * Method for displaying stats of particular code linked to user
     *
     * @param code shortened url we want to look for
     * @param userId id of an authenticated user
     * @return {@link StatsUrlDto} dto of stats from found url
     */
    public StatsUrlDto getStatsUrl(String code, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));


        Url url = urlRepository.findByCodeAndUser_Id(code, userId)
                .orElseThrow(() -> new NoSuchElementException("link not found"));

        return urlMapper.urlToStatsDto(url);
    }


    /**
     * Page of all links in database
     *
     * @param pageable page request
     * @return paged urls
     */
    public Page<Url> getAllLinks(Pageable pageable) {
        return urlRepository.findAll(pageable);
    }


    /**
     * Method for showing authenticated user's paginated links
     *
     * @param pageable page request
     * @param userId id of an authenticated user
     * @return {@link StatsUrlDto} dto of stats for each url
     */
    public Page<StatsUrlDto> showMyLinks(Pageable pageable, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        Page<Url> urls = urlRepository.findAllByUser_Id(userId, pageable);

        return urlMapper.urlToStatsPageDto(urls);
    }
}
