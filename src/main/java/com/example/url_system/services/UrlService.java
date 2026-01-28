package com.example.url_system.services;

import com.example.url_system.dtos.*;
import com.example.url_system.exceptions.ResponseAlreadyBeingProcessed;
import com.example.url_system.exceptions.UrlExpiredException;
import com.example.url_system.models.IdempotencyKeys;
import com.example.url_system.models.IdempotencyStatus;
import com.example.url_system.models.Url;
import com.example.url_system.models.User;
import com.example.url_system.repositories.IdempotencyKeyRepository;
import com.example.url_system.repositories.UrlRepository;
import com.example.url_system.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;


@Service
public class UrlService {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 8;
    private static final String OP_CREATE_URL = "CREATE_URL";


    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;
    private final UserRepository userRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private final SecureRandom random = new SecureRandom();

    public UrlService(UrlRepository urlRepository, UrlMapper urlMapper, UserRepository userRepository, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.urlRepository = urlRepository;
        this.urlMapper = urlMapper;
        this.userRepository = userRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }


    /**
     * Method for creating a shortened link and adding it to users account (optional)
     *
     * @param createUrlRequest {@link CreateUrlRequest} dto to create link
     * @param userId id of an authenticated user
     * @param idempotencyKey idempotency key from client.
     * @return {@link CreateResponseUrlDto} dto with created url data
     */
    @Transactional
    public CreateResponseUrlDto create(CreateUrlRequest createUrlRequest, Long userId, String idempotencyKey) {
        IdempotencyKeys idem;
        boolean isRetry = false;

        try {
            idem = idempotencyKeyRepository.save(
                    new IdempotencyKeys(OP_CREATE_URL, idempotencyKey)
            );
        } catch (DataIntegrityViolationException e) {
            isRetry = true;
            idem = idempotencyKeyRepository
                    .findByOperationAndIdempotencyKey(OP_CREATE_URL, idempotencyKey)
                    .orElseThrow(() -> new NoSuchElementException("Idempotency Key Not Found"));
        }

        // ---------- RETRY PATH ----------
        if (isRetry) {
            if (idem.getIdempotencyStatus() == IdempotencyStatus.COMPLETED) {
                Url url = urlRepository.findById(idem.getCreatedUrlId())
                        .orElseThrow(() -> new NoSuchElementException("Url Not Found"));
                return urlMapper.urlToCreateDto(url);
            }

            if (idem.getIdempotencyStatus() == IdempotencyStatus.IN_PROGRESS) {
                throw new ResponseAlreadyBeingProcessed(
                        "Request is already being processed"
                );
            }
        }

        // ---------- WINNER PATH ----------
        if (createUrlRequest.expiredAt() != null &&
                createUrlRequest.expiredAt().isBefore(Instant.now())) {
            throw new UrlExpiredException("Url expired");
        }

        String code = generateUniqueCode();
        Url url = new Url(code, createUrlRequest.longUrl(), createUrlRequest.expiredAt());


        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            user.addUrl(url);
            url.setUser(user);
        }

        Url saved = urlRepository.save(url);

        idem.setCreatedUrlId(saved.getId());
        idem.setIdempotencyStatus(IdempotencyStatus.COMPLETED);

        return urlMapper.urlToCreateDto(saved);
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
    @Transactional(readOnly = true)
    public StatsUrlDto getStatsUrl(String code, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));


        Url url = urlRepository.findByCodeAndUser_Id(code, userId)
                .orElseThrow(() -> new NoSuchElementException("Url not found"));

        return urlMapper.urlToStatsDto(url);
    }


    /**
     * Page of all links in database
     *
     * @param pageable page request
     * @return paged urls
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Page<StatsUrlDto> showMyLinks(Pageable pageable, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        Page<Url> urls = urlRepository.findAllByUser_Id(userId, pageable);

        return urlMapper.urlToStatsPageDto(urls);
    }
}
