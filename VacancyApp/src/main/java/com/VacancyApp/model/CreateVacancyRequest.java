package com.VacancyApp.model;

import java.time.LocalDateTime;

/** Request body for POST /vacancies (HR creates a vacancy). */
public record CreateVacancyRequest(
        String title,
        String description,
        String department,
        LocalDateTime expiresAt
) {
}
