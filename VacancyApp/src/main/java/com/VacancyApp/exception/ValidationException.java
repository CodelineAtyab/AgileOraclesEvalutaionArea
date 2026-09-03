package com.VacancyApp.exception;

/** Thrown when request input is missing or invalid -> HTTP 400. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
