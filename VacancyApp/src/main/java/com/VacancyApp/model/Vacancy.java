package com.VacancyApp.model;

import java.time.LocalDateTime;

/** A live vacancy as returned by Oracle's get_available_vacancies. */
public record Vacancy(
        Long jobId,
        String title,
        String description,
        String department,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        String status
) {
}
