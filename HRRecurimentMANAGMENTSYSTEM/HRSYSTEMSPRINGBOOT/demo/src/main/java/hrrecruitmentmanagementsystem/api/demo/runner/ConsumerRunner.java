package hrrecruitmentmanagementsystem.api.demo.runner;


import hrrecruitmentmanagementsystem.api.demo.mpdel.NotificationRecord;
import hrrecruitmentmanagementsystem.api.demo.repository.NotificationRepository;
import hrrecruitmentmanagementsystem.api.demo.service.SlackNotifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsumerRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConsumerRunner.class);

    private final NotificationRepository notificationRepository;
    private final SlackNotifierService slackNotifierService;

    public ConsumerRunner(NotificationRepository notificationRepository,
                          SlackNotifierService slackNotifierService) {
        this.notificationRepository = notificationRepository;
        this.slackNotifierService = slackNotifierService;
    }

    @Override
    public void run(String... args) {
        log.info("Starting vacancy notification consumer...");

        List<NotificationRecord> pending = notificationRepository.fetchPendingNotifications();
        log.info("Found {} pending notification(s).", pending.size());

        int sentCount = 0;
        int failedCount = 0;

        for (NotificationRecord record : pending) {
            String message = "*" + record.getSubject() + "*\n" + record.getBody();

            try {
                slackNotifierService.sendMessage(message);
                notificationRepository.markAsSent(record.getNotificationId());
                sentCount++;
                log.info("Sent and marked notification {}", record.getNotificationId());

            } catch (Exception e) {

                failedCount++;
                log.error("Failed to send notification {} - will remain pending: {}",
                        record.getNotificationId(), e.getMessage());
            }
        }

        log.info("Consumer finished. Sent: {}, Failed: {}, Total: {}",
                sentCount, failedCount, pending.size());
    }
}