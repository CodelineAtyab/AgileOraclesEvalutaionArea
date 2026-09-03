package com.VacancyApp.model;

import java.util.List;

/** Everything get_vacancy_dashboard returns, in one object (3 cursors combined). */
public record DashboardSnapshot(
        DashboardSummary summary,
        List<Vacancy> jobs,
        List<ApplicationCount> applicationCounts
) {
}
