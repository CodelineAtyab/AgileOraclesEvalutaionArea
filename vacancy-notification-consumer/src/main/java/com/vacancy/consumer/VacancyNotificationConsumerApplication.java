package com.vacancy.consumer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VacancyNotificationConsumerApplication {

    public static void main(String[] args) {
        // Start Spring, run the CommandLineRunner, then close the application cleanly.
        ConfigurableApplicationContext context = SpringApplication.run(VacancyNotificationConsumerApplication.class, args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }

    @Bean
    CommandLineRunner run(NotificationConsumer notificationConsumer) {
        // CommandLineRunner makes this app run once automatically after startup.
        return args -> notificationConsumer.processPendingNotifications();
    }

}
