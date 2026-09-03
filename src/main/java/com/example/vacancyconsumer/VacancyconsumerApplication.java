package com.example.vacancyconsumer;

import com.example.vacancyconsumer.service.NotificationConsumerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class VacancyconsumerApplication implements CommandLineRunner {

    private final NotificationConsumerService consumerService;

    public VacancyconsumerApplication(
            NotificationConsumerService consumerService
    ) {
        this.consumerService = consumerService;
    }

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(
                    VacancyconsumerApplication.class,
                    args
            );

            int exitCode = SpringApplication.exit(context);
            System.exit(exitCode);
        } catch (Exception exception) {
            System.err.println(
                    "Vacancy notification consumer failed: "
                            + exception.getMessage()
            );
            System.exit(1);
        }
    }

    @Override
    public void run(String... args) throws Exception {
        consumerService.drainNotifications();
    }
}
