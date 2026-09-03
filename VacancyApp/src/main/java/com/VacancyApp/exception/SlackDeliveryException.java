package com.VacancyApp.exception;

/** Thrown when a Slack webhook request fails (non-2xx response or I/O error). */
public class SlackDeliveryException extends RuntimeException {
    public SlackDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SlackDeliveryException(String message) {
        super(message);
    }
}
