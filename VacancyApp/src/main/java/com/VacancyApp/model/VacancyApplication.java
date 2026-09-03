package com.VacancyApp.model;

import java.time.LocalDateTime;

/** A candidate application row (job_applications). */
public record VacancyApplication(
        Long applicationId,
        Long jobId,
        String applicantName,
        String applicantEmail,
        LocalDateTime appliedAt
) {
}
