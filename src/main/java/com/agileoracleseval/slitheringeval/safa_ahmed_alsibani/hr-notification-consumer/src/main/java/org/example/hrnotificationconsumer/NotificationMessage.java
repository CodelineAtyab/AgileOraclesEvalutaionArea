package org.example.hrnotificationconsumer;

import java.time.LocalDateTime;

public record NotificationMessage(
        long id,
        String subject,
        String body,
        LocalDateTime createdAt
) {
}
