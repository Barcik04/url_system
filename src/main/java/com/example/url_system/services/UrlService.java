package com.example.url_system.services;

import com.example.url_system.dtos.*;

import com.example.url_system.exceptions.ResponseAlreadyBeingProcessed;
import com.example.url_system.exceptions.UrlExpiredException;
import com.example.url_system.models.*;
import com.example.url_system.repositories.IdempotencyKeyRepository;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UrlRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.utils.dynamicFiltering.UrlFilter;
import com.example.url_system.utils.dynamicFiltering.UrlSpecs;
import com.example.url_system.utils.redis.RedisCacheClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.hibernate.AssertionFailure;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import java.util.NoSuchElementException;
import java.util.Set;


@Service
public class UrlService {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 8;
    private static final String OP_CREATE_URL = "CREATE_URL";


    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;
    private final UserRepository userRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
//    private final RedisCacheClient redisCacheClient;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;


    private final SecureRandom random = new SecureRandom();

    public UrlService(UrlRepository urlRepository, UrlMapper urlMapper, UserRepository userRepository, IdempotencyKeyRepository idempotencyKeyRepository /*RedisCacheClient redisCacheClient*/, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper, Clock clock) {
        this.urlRepository = urlRepository;
        this.urlMapper = urlMapper;
        this.userRepository = userRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
//        this.redisCacheClient = redisCacheClient;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }


    /**
     * Method for creating a shortened link and adding it to users account (optional)
     *
     * @param createUrlRequest {@link CreateUrlRequest} dto to create link
     * @param userId id of an authenticated user
     * @param idempotencyKey idempotency key from client.
     * @return {@link CreateResponseUrlDto} dto with created url data
     */
    @CircuitBreaker(name = "baseService")
    @Transactional
    public CreateResponseUrlDto create(CreateUrlRequest createUrlRequest, Long userId, String idempotencyKey) {
        IdempotencyKeys idem;
        boolean isRetry = false;

        try {
            idem = idempotencyKeyRepository.save(
                    new IdempotencyKeys(OP_CREATE_URL, idempotencyKey)
            );
        } catch (AssertionFailure | DataIntegrityViolationException e) {
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

        if (userId != null && createUrlRequest.expiredAt() != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            OutboxPayloadDto outboxPayloadDto = new OutboxPayloadDto(
                    user.getUsername(),
                    saved.getCode() + " expired",
                    "One of your urls: " + createUrlRequest.longUrl() + " expired " + createUrlRequest.expiredAt()
            );

            JsonNode jsonPayload = objectMapper.valueToTree(outboxPayloadDto);

            outboxEventRepository.save(new OutboxEvent(
                    "EMAIL_SEND_REQUESTED",
                    jsonPayload,
                    OutboxEvent.Status.NEW,
                    null,
                    createUrlRequest.expiredAt()
            ));
        }

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
    @CircuitBreaker(name = "baseService")
    @Retry(name = "baseService")
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
     * Method for displaying stats of particular code linked to user. Redis cache used TTL 30 seconds
     *
     * @param code shortened url we want to look for
     * @param userId id of an authenticated user
     * @return {@link StatsUrlDto} dto of stats from found url
     */
    @Retry(name = "baseService")
    @CircuitBreaker(name = "baseService")
    @Transactional(readOnly = true)
    public StatsUrlDto getStatsUrl(String code, Long userId) {

//        String cacheKey = "stats:user:" + userId + ":code:" + code;
//
//        return redisCacheClient.get(cacheKey, StatsUrlDto.class)
//                        .orElseGet(() -> {
//                            userRepository.findById(userId)
//                                    .orElseThrow(() -> new NoSuchElementException("User not found"));
//
//
//                            Url url = urlRepository.findByCodeAndUser_Id(code, userId)
//                                    .orElseThrow(() -> new NoSuchElementException("Url not found"));
//
//                            StatsUrlDto dto = urlMapper.urlToStatsDto(url);
//
//                            redisCacheClient.set(cacheKey, dto, Duration.ofSeconds(30));
//
//                            return dto;
//                        });

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
    @Bulkhead(name = "baseService")
    @CircuitBreaker(name = "baseService")
    @Transactional(readOnly = true)
    public Page<Url> getAllLinks(Pageable pageable, UrlFilter filter, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        if (filter == null) {
            return urlRepository.findAll(pageable);
        }

        Specification<Url> spec = Specification.allOf();

        Specification<Url> text = UrlSpecs.textSearch(filter.q());
        if (text != null) {
            spec = spec.and(text);
        }

        Specification<Url> exp = UrlSpecs.expired(filter.expired(), clock);
        if (exp != null) {
            spec = spec.and(exp);
        }


        Pageable safePageable = sanitizeSort(pageable);
        return urlRepository.findAll(spec, safePageable);
    }





    /**
     * Method for showing authenticated user's paginated links
     *
     * @param pageable page request
     * @param userId id of an authenticated user
     *
     * @return {@link StatsUrlDto} dto of stats for each url
     */
    @Bulkhead(name = "baseService")
    @CircuitBreaker(name = "baseService")
    @Transactional(readOnly = true)
    public Page<StatsUrlDto> showMyLinks(Pageable pageable, Long userId, UrlFilter filter) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        if (filter == null) {
            return urlMapper.urlToStatsPageDto(urlRepository.findAllByUser_Id(userId, pageable));
        }

        Specification<Url> spec = Specification.where(UrlSpecs.belongsToUser(userId));

        Specification<Url> text = UrlSpecs.textSearch(filter.q());
        if (text != null) {
            spec = spec.and(text);
        }

        Specification<Url> exp = UrlSpecs.expired(filter.expired(), clock);
        if (exp != null) {
            spec = spec.and(exp);
        }


        Pageable safePageable = sanitizeSort(pageable);
        Page<Url> urls = urlRepository.findAll(spec, safePageable);

        return urlMapper.urlToStatsPageDto(urls);
    }






    private Pageable sanitizeSort(Pageable pageable) {
        var allowed = Set.of("clicks", "expiresAt", "createdAt");

        var orders = pageable.getSort().stream()
                .filter(o -> allowed.contains(o.getProperty()))
                .toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }


    /**
     * Method for deleting urls
     *
     * @param userId Id of an authenticated user
     * @param code url code (shortUrl)
     */
    @Transactional
    public void deleteUrl(Long userId, String code) {
        if (userId == null || code == null || code.isBlank()) {
            throw new IllegalArgumentException("userId/code can't be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        Url url = urlRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("url not found"));


        if (user.getRole().equals(Role.ADMIN)) {
            urlRepository.deleteByCode(url.getCode());
        } else {
            if (url.getUser() == null) {
                throw new IllegalStateException("This url does not belong to the user");
            }

            if ((!url.getUser().getId().equals(user.getId()))) {
                throw new IllegalArgumentException("No permission to delete url");
            } else {
                urlRepository.deleteByCodeAndUser_Id(url.getCode(), user.getId());
            }
        }
    }


    /**
     * Method for patching urls
     *
     * @param userId id of an authenticated user
     * @param code shortUrl of url to patch
     * @param patchUrlDto Dto request of url to patch
     * @return {@link UrlResponseDto}
     */
    public UrlResponseDto patchUrl(Long userId, String code, PatchUrlDto patchUrlDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        Url url = urlRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Url not found"));

        if (user.getRole().equals(Role.ADMIN)) {
            if (patchUrlDto.longUrl() != null && !patchUrlDto.longUrl().isBlank()) { url.setLongUrl(patchUrlDto.longUrl()); }
            if (patchUrlDto.expiredAt() != null) { url.setExpiresAt(patchUrlDto.expiredAt()); }
            return urlMapper.urlToDto(urlRepository.save(url));
        }

        if (urlRepository.findAllByUser_Id(userId).stream().noneMatch(a -> a.getCode().equals(code))) {
            throw new NoSuchElementException("Url with code {" + code + "} does not belong to the user");
        }

        if (patchUrlDto.longUrl() != null && !patchUrlDto.longUrl().isBlank()) { url.setLongUrl(patchUrlDto.longUrl()); }
        if (patchUrlDto.expiredAt() != null) { url.setExpiresAt(patchUrlDto.expiredAt()); }
        return urlMapper.urlToDto(urlRepository.save(url));
    }
}
