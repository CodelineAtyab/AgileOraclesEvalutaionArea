package com.VacancyApp.service;

import com.VacancyApp.exception.SlackDeliveryException;
import com.VacancyApp.model.DashboardSnapshot;
import com.VacancyApp.model.Notification;
import com.VacancyApp.model.Vacancy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Orchestrates the scheduled work: every poll it consumes the Oracle procedures and, if pending
 * notifications reach the threshold, sends the digest immediately; the reporting cycle sends both
 * Slack messages. Oracle owns all the underlying business logic.
 */
@Service
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);
    private static final int DIGEST_MAX = 7;

    private final com.VacancyApp.repository.VacancyRepository vacancyRepository;
    private final com.VacancyApp.repository.NotificationRepository notificationRepository;
    private final SlackService slackService;
    private final int notificationThreshold;

    /** Serializes digest sending so the immediate (>=threshold) path and the timed path never overlap. */
    private final ReentrantLock digestLock = new ReentrantLock();

    public ReportingService(com.VacancyApp.repository.VacancyRepository vacancyRepository,
                            com.VacancyApp.repository.NotificationRepository notificationRepository,
                            SlackService slackService,
                            @Value("${reporting.notification-threshold:7}") int notificationThreshold) {
        this.vacancyRepository = vacancyRepository;
        this.notificationRepository = notificationRepository;
        this.slackService = slackService;
        this.notificationThreshold = notificationThreshold;
    }

    /** 5-minute poll: consume Oracle procedures; send the digest early if pending has piled up. */
    public void pollCycle() {
        List<Vacancy> available = vacancyRepository.findAvailable();
        DashboardSnapshot dashboard = vacancyRepository.fetchDashboard();
        List<Notification> pending = notificationRepository.findPending();

        log.info("Poll: {} available vacancies, {} pending notifications, {} total applications",
                available.size(), pending.size(), dashboard.summary().totalApplications());

        if (pending.size() >= notificationThreshold) {
            log.info("Pending notifications ({}) reached threshold ({}) — sending digest immediately",
                    pending.size(), notificationThreshold);
            sendDigest();
        }
    }

    /** 15-minute report: send the dashboard and the notification digest to Slack. */
    public void reportCycle() {
        sendDashboard();
        sendDigest();
    }

    private void sendDashboard() {
        DashboardSnapshot dashboard = vacancyRepository.fetchDashboard();
        try {
            slackService.sendDashboard(dashboard);
            log.info("Dashboard message sent ({} vacancies)", dashboard.jobs().size());
        } catch (SlackDeliveryException e) {
            log.error("Failed to send vacancy dashboard to Slack: {}", e.getMessage(), e);
        }
    }

    /**
     * Sends up to {@value #DIGEST_MAX} pending notifications, each as its own Slack message, marking
     * each SENT on success or FAILED on error independently. Locked so overlapping triggers cannot
     * send the same batch twice.
     */
    private void sendDigest() {
        digestLock.lock();
        try {
            List<Notification> pending = notificationRepository.findPending();
            if (pending.isEmpty()) {
                log.debug("No pending notifications — digest skipped");
                return;
            }
            List<Notification> batch = pending.subList(0, Math.min(DIGEST_MAX, pending.size()));
            int sent = 0;
            int failed = 0;
            for (Notification n : batch) {
                try {
                    slackService.sendNotification(n);
                    notificationRepository.markSent(n.notificationId());
                    sent++;
                } catch (SlackDeliveryException e) {
                    notificationRepository.markFailed(n.notificationId());
                    failed++;
                    log.error("Failed to send notification #{}; marked FAILED: {}",
                            n.notificationId(), e.getMessage(), e);
                }
            }
            log.info("Notification digest processed: {} sent, {} failed (of {} pending)",
                    sent, failed, pending.size());
        } finally {
            digestLock.unlock();
        }
    }
}
