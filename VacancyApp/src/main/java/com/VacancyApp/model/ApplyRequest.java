package com.VacancyApp.model;

/** Request body for POST /vacancies/{jobId}/applications. */
public record ApplyRequest(
        String applicantName,
        String applicantEmail
) {
}
