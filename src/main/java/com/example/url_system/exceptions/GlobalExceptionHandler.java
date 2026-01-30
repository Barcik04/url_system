package com.example.url_system.exceptions;

import jakarta.persistence.ElementCollection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.AssertionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException e,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String message = e.getReason() != null
                ? e.getReason()
                : status.getReasonPhrase();

        if (status.is4xxClientError()) {
            log.warn("Client error {}: {}", status.value(), message);
        } else {
            log.error("Server error {}: {}", status.value(), message, e);
        }

        ApiError apiError = new ApiError(
                message,
                status.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(apiError, status);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(
            NoResourceFoundException e,
            HttpServletRequest request) {


        ApiError apiError = new ApiError(
                e.getMessage(),
                404,
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.debug("NOT_FOUND method={} path={} status={} message={}",
                request.getMethod(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value(),
                e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }



    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorizationDeniedException(
            AuthorizationDeniedException e,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                e.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Acess denied {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }


    @ExceptionHandler(ResponseAlreadyBeingProcessed.class)
    public ResponseEntity<ApiError> handleResponseAlreadyBeingProcessed(
            ResponseAlreadyBeingProcessed e,
            HttpServletRequest request
    ) {

        ApiError apiError = new ApiError(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Request already being processed {} {} {} {}", request.getMethod(), request.getRequestURI(), e.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }




    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException ex,
                                                        HttpServletRequest request) {
        ApiError apiError = new ApiError(
                "Missing " + ex.getHeaderName(),
                HttpStatus.BAD_REQUEST.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );


        log.warn("Missing headers at: {} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }


    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public ResponseEntity<ApiError> handleCannotGetJdbcConnection(
            CannotGetJdbcConnectionException ex,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                "Database unavailable",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.error("Cannot reach the Database{} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiError);
    }




    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiError> handleDataAccessResourceFailure(
            DataAccessResourceFailureException ex,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                "DB_UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.error("Db unavailable{} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(apiError);
    }



    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<ApiError> handleCannotAcquireLock(
            CannotAcquireLockException ex,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                "Database lock conflict",
                HttpStatus.CONFLICT.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.error("Database lock conflict {} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }


    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<ApiError> handleQueryTimeout(
            QueryTimeoutException ex,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                "Database timeout",
                HttpStatus.GATEWAY_TIMEOUT.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.error("Database timeout {} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return  ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(apiError);
    }


    @ExceptionHandler(org.hibernate.QueryTimeoutException.class)
    public ResponseEntity<ApiError> handleQueryTimeoutHibernate(
            QueryTimeoutException ex,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                "Database timeout",
                HttpStatus.GATEWAY_TIMEOUT.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.error("Database timeout hibernate {} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return  ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(apiError);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiError> handleTransactionSystemException(
            TransactionSystemException ex,
            HttpServletRequest request
    ) {
        ApiError apiError = new ApiError(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                ZonedDateTime.now(),
                request.getRequestURI(),
                null
        );

        log.warn("Credentials invalid: {} {} {} {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), apiError.status());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}
