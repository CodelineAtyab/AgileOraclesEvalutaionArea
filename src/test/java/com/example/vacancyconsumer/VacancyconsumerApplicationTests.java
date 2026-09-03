package com.example.vacancyconsumer;

import com.example.vacancyconsumer.model.NotificationMessage;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VacancyconsumerApplicationTests {

    @Test
    void notificationMessageKeepsDatabaseValues() {
        Timestamp createdAt = Timestamp.valueOf("2026-09-03 10:00:00");

        NotificationMessage message = new NotificationMessage(
                1L,
                "Test subject",
                "Test body",
                createdAt
        );

        assertEquals(1L, message.id());
        assertEquals("Test subject", message.subject());
        assertEquals("Test body", message.body());
        assertEquals(createdAt, message.createdAt());
    }
}
