package notification_consumer;

import java.time.LocalDateTime;

    public record Notification(
            long notificationId,
            String subject,
            String body,
            LocalDateTime createdAt
    ) {
    }
