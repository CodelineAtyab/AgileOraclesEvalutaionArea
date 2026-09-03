package com.example.vacancyconsumer.service;

import com.example.vacancyconsumer.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NotificationConsumerService {

    private final NotificationRepository repository;
    private final SlackService slackService;

    public NotificationConsumerService(
            NotificationRepository repository,
            SlackService slackService
    ) {
        this.repository = repository;
        this.slackService = slackService;
    }

    /**
     * Opens the database result set once, processes it in order, then returns.
     * One bad Slack delivery does not stop later rows from being attempted.
     */
    public void drainNotifications() throws Exception {

        AtomicInteger processed = new AtomicInteger();
        AtomicInteger sent = new AtomicInteger();
        AtomicInteger pending = new AtomicInteger();

        repository.consumePending(notification -> {
            processed.incrementAndGet();

            System.out.println("----------------------------------------");
            System.out.println("Processing notification " + notification.id());
            System.out.println("Subject: " + notification.subject());
            System.out.println("Created: " + notification.createdAt());

            boolean delivered = slackService.sendNotification(notification);

            if (delivered) {
                try {
                    repository.markAsSent(notification.id());
                    sent.incrementAndGet();
                    System.out.println(
                            "Notification " + notification.id() + " marked SENT."
                    );
                } catch (Exception exception) {
                    pending.incrementAndGet();
                    System.err.println(
                            "Slack succeeded, but Oracle could not mark notification "
                                    + notification.id()
                                    + " SENT: "
                                    + exception.getMessage()
                    );
                }
            } else {
                pending.incrementAndGet();
                System.out.println(
                        "Notification " + notification.id() + " remains PENDING."
                );
            }
        });

        System.out.println("========================================");
        System.out.println("Consumer finished.");
        System.out.println("Processed: " + processed.get());
        System.out.println("Marked SENT: " + sent.get());
        System.out.println("Still PENDING: " + pending.get());
    }
}
