package com.VacancyApp.model;

/** Summary counts from the first cursor of get_vacancy_dashboard. */
public record DashboardSummary(
        long activeJobs,
        long totalApplications,
        long newNotifications
) {
}
