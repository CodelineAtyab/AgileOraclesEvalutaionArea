package com.VacancyApp.exception;

/** Thrown when a referenced resource (e.g. a vacancy) does not exist -> HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
