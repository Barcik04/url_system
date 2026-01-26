package com.example.url_system.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception e,
            HttpServletRequest request) {


        ApiError apiError = new ApiError(
                e.getClass().getName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.error("Unexpected server error on {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ApiError> handleUrlExpiredException(
            UrlExpiredException e,
            HttpServletRequest request
    ) {

        ApiError apiError = new ApiError(
                e.getMessage(),
                HttpStatus.GONE.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Url expired on {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.GONE).body(apiError);
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElementException(
            NoSuchElementException e,
            HttpServletRequest request
    ) {

        ApiError apiError = new ApiError(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Element not found on {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Method argument not valid on {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Constraint violation on {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}
