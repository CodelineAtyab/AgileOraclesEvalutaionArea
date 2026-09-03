// Read pending notifications and verify the Slack connection

package notification_consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationConsumer implements CommandLineRunner {

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
    public void run(String... args) throws Exception {
        List<Notification> notifications =
                notificationRepository.getPendingNotifications();

        System.out.println(
                "Pending notifications: " + notifications.size()
        );

        for (Notification notification : notifications) {
            try {
                String slackMessage =
                        "*" + notification.subject() + "*\n"
                                + notification.body();

                slackClient.sendMessage(slackMessage);

                notificationRepository.markNotificationSent(
                        notification.notificationId()
                );

                System.out.println(
                        "Sent notification: "
                                + notification.notificationId()
                );
            } catch (Exception exception) {
                System.err.println(
                        "Failed notification: "
                                + notification.notificationId()
                                + " | "
                                + exception.getMessage()
                );
            }
        }
    }
}
