package com.hrrecruitment.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository repository;
    private final SlackNotificationSender slackSender;

    public NotificationService(NotificationRepository repository, SlackNotificationSender slackSender) {
        this.repository = repository;
        this.slackSender = slackSender;
    }

    @Scheduled(fixedDelayString = "${notification.check-interval-ms:60000}")
    public void processPendingNotifications() {
        for (Notification notification : repository.findPendingNotifications()) {
            try {
                slackSender.send(notification);
                repository.markSent(notification.getNotificationId());
                log.info("Sent notification {} to Slack", notification.getNotificationId());
            } catch (Exception exception) {
                log.warn("Could not send notification {} to Slack", notification.getNotificationId(), exception);
                try {
                    repository.recordFailure(notification.getNotificationId());
                } catch (Exception failureException) {
                    log.error("Could not record failure for notification {}", notification.getNotificationId(), failureException);
                }
            }
        }
    }
}
