package com.VacancyApp.controller;

import com.VacancyApp.exception.ConflictException;
import com.VacancyApp.exception.NotFoundException;
import com.VacancyApp.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/** Translates service exceptions into clean JSON error responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({ValidationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception e) {
        String message = (e instanceof HttpMessageNotReadableException)
                ? "Malformed or missing request body" : e.getMessage();
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {
        log.error("Unexpected error handling request: {}", e.getMessage(), e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message == null ? "" : message
        );
        return ResponseEntity.status(status).body(body);
    }
}
