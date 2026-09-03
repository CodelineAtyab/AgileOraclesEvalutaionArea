package com.example.vacancyconsumer.model;

import java.sql.Timestamp;

public record NotificationMessage(
        long id,
        String subject,
        String body,
        Timestamp createdAt
) {
}