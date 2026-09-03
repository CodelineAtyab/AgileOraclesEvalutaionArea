package com.vacancyloop.vacancy_loop_consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// runs once when the application starts: drains the queue, then the app exits
@Component
public class NotificationRunner implements CommandLineRunner {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting to drain pending notifications...");
        notificationService.drainPendingNotifications();
        System.out.println("Done. All pending notifications sent.");
    }
}