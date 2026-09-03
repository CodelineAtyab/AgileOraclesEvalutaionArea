package com.VacancyApp.model;

import java.time.LocalDateTime;

/** A notification row as returned by Oracle's get_pending_notifications. */
public record Notification(
        Long notificationId,
        String subject,
        String body,
        LocalDateTime createdAt,
        String status,
        LocalDateTime sentAt
) {
}
