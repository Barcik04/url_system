package com.example.url_system.exceptions;

import java.time.ZonedDateTime;
import java.util.List;


public record ApiError(
        String message,
        int status,
        ZonedDateTime timestamp,
        String path,
        List<ApiFieldError> errors
) {
    public record ApiFieldError(
            String field,
            String rejectedValue,
            String message
    ) {}}