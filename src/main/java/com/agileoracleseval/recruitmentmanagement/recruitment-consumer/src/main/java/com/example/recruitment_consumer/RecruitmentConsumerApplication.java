package com.example.recruitment_consumer;

import com.example.recruitment_consumer.service.NotificationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecruitmentConsumerApplication
		implements CommandLineRunner {

	private final NotificationService notificationService;

	public RecruitmentConsumerApplication(
			NotificationService notificationService
	) {
		this.notificationService = notificationService;
	}

	public static void main(String[] args) {

		SpringApplication.run(
				RecruitmentConsumerApplication.class,
				args
		);
	}

	@Override
	public void run(String... args) {
		System.out.println("Starting notification consumer...");

		notificationService.consumePendingNotifications();

		System.out.println("Notification consumer finished.");
	}
}
