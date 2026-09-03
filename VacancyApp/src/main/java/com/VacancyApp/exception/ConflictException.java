package com.VacancyApp.exception;

/** Thrown when a request conflicts with resource state (e.g. applying to a closed job) -> HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
