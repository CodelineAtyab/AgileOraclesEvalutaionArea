package com.vacancyloop.consumer;

import com.vacancyloop.consumer.service.NotificationConsumerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRunner {

    private final NotificationConsumerService notificationConsumerService;

    public ConsumerRunner(
            NotificationConsumerService notificationConsumerService
    ) {
        this.notificationConsumerService = notificationConsumerService;
    }

    @Scheduled(
            fixedDelayString = "${consumer.poll-interval-ms:60000}",
            initialDelayString = "${consumer.initial-delay-ms:3000}"
    )
    public void run() {

        notificationConsumerService.processPendingNotifications();

        System.out.println(
                "Vacancy Loop notification check completed."
        );
    }
}