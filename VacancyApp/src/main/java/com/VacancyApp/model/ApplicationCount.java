package com.VacancyApp.model;

/** Per-vacancy application count from the third cursor of get_vacancy_dashboard. */
public record ApplicationCount(
        Long jobId,
        String title,
        long applicationCount
) {
}
