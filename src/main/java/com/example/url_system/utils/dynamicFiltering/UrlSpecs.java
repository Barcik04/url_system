package com.example.url_system.utils.dynamicFiltering;

import com.example.url_system.models.Url;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;

public class UrlSpecs {

    public static Specification<Url> belongsToUser(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Url> textSearch(String q) {
        if (q == null || q.isBlank()) return null;

        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("longUrl")), like),
                cb.like(cb.lower(root.get("code")), like)
        );
    }

    public static Specification<Url> expired(Boolean expired, Clock clock) {
        if (expired == null) return null;

        Instant now = clock.instant();
        if (expired) {
            return (root, query, cb) -> cb.and(
                    cb.isNotNull(root.get("expiresAt")),
                    cb.lessThanOrEqualTo(root.get("expiresAt"), now)
            );
        } else {
            return (root, query, cb) -> cb.or(
                    cb.isNull(root.get("expiresAt")),
                    cb.greaterThan(root.get("expiresAt"), now)
            );
        }
    }
}
