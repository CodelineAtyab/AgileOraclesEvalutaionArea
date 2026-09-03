package com.vacancyloop.consumer.service;

import com.vacancyloop.consumer.model.NotificationMessage;
import com.vacancyloop.consumer.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationConsumerService {

    private final NotificationRepository notificationRepository;
    private final SlackService slackService;

    public NotificationConsumerService(
            NotificationRepository notificationRepository,
            SlackService slackService) {

        this.notificationRepository = notificationRepository;
        this.slackService = slackService;
    }

    public void processPendingNotifications() {

        List<NotificationMessage> notifications =
                notificationRepository.getPendingNotifications();

        System.out.println(
                "Pending notifications: " + notifications.size()
        );

        for (NotificationMessage notification : notifications) {

            try {

                String message =
                        notification.getSubject()
                                + "\n"
                                + notification.getBody();

                slackService.sendMessage(message);

                notificationRepository.updateDeliveryStatus(
                        notification.getNotificationId(),
                        "SENT",
                        null
                );

                System.out.println(
                        "Notification "
                                + notification.getNotificationId()
                                + " sent successfully."
                );

            } catch (Exception e) {

                notificationRepository.updateDeliveryStatus(
                        notification.getNotificationId(),
                        "FAILED",
                        e.getMessage()
                );

                System.out.println(
                        "Notification "
                                + notification.getNotificationId()
                                + " failed: "
                                + e.getMessage()
                );
            }
        }
    }
}