package com.VacancyApp.scheduler;

import com.VacancyApp.service.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Wires the two scheduled cycles to the ReportingService. Rates are configurable. */
@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private final ReportingService reportingService;

    public ScheduledTasks(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /** Every 5 minutes: consume Oracle procedures (and send digest early if it has piled up). */
    @Scheduled(fixedRateString = "${reporting.poll-rate-ms:300000}", initialDelay = 15000)
    public void poll() {
        try {
            reportingService.pollCycle();
        } catch (Exception e) {
            log.error("Poll cycle failed: {}", e.getMessage(), e);
        }
    }

    /** Every 15 minutes: send the dashboard and notification digest to Slack. */
    @Scheduled(fixedRateString = "${reporting.report-rate-ms:900000}", initialDelay = 30000)
    public void report() {
        try {
            reportingService.reportCycle();
        } catch (Exception e) {
            log.error("Report cycle failed: {}", e.getMessage(), e);
        }
    }
}
