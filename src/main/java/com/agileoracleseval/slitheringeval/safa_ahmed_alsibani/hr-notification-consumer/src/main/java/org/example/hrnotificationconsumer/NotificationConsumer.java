package org.example.hrnotificationconsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationConsumer implements CommandLineRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationRepository notificationRepository;
    private final SlackClient slackClient;

    public NotificationConsumer(
            NotificationRepository notificationRepository,
            SlackClient slackClient
    ) {
        this.notificationRepository = notificationRepository;
        this.slackClient = slackClient;
    }

    @Override
    public void run(String... args) {
        List<NotificationMessage> notifications =
                notificationRepository.findPendingNotifications();

        if (notifications.isEmpty()) {
            logger.info("No pending notifications found.");
            return;
        }

        logger.info(
                "Found {} pending notification(s).",
                notifications.size()
        );

        for (NotificationMessage notification : notifications) {
            try {
                slackClient.send(notification);
                notificationRepository.markAsSent(notification.id());

                logger.info(
                        "Notification {} sent successfully.",
                        notification.id()
                );
            } catch (Exception exception) {
                logger.error(
                        "Failed to send notification {}. It remains PENDING.",
                        notification.id(),
                        exception
                );

                break;
            }
        }
    }
}