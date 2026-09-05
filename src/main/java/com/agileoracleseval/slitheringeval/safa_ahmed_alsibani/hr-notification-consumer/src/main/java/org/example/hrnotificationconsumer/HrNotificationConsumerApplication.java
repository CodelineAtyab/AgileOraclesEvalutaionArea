package org.example.hrnotificationconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HrNotificationConsumerApplication {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context =
                     SpringApplication.run(
                             HrNotificationConsumerApplication.class,
                             args
                     )) {
            // The CommandLineRunner sends all pending notifications.
        }
    }
}