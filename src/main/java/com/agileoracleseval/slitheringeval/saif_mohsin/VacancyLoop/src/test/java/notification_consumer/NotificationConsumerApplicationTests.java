// Verify the notification data object without external services

package notification_consumer;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationConsumerApplicationTests {

	@Test
	void notificationStoresItsValues() {
		LocalDateTime createdAt = LocalDateTime.of(2026, 9, 3, 15, 0);

		Notification notification = new Notification(
				1L,
				"Test subject",
				"Test body",
				createdAt
		);

		assertEquals(1L, notification.notificationId());
		assertEquals("Test subject", notification.subject());
		assertEquals("Test body", notification.body());
		assertEquals(createdAt, notification.createdAt());
	}

}
