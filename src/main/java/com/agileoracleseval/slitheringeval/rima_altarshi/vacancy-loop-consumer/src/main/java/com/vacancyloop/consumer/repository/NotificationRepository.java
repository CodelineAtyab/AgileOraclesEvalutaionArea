package com.vacancyloop.consumer.repository;

import com.vacancyloop.consumer.model.NotificationMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<NotificationMessage> getPendingNotifications() {

        List<NotificationMessage> notifications = new ArrayList<>();

        jdbcTemplate.execute((Connection connection) -> {

            try (CallableStatement statement =
                         connection.prepareCall(
                                 "{call GET_PENDING_NOTIFICATIONS(?)}"
                         )) {

                statement.registerOutParameter(
                        1,
                        Types.REF_CURSOR
                );

                statement.execute();

                try (ResultSet resultSet =
                             (ResultSet) statement.getObject(1)) {

                    while (resultSet.next()) {

                        NotificationMessage notification =
                                new NotificationMessage();

                        notification.setNotificationId(
                                resultSet.getLong("notification_id")
                        );

                        notification.setSubject(
                                resultSet.getString("subject")
                        );

                        notification.setBody(
                                resultSet.getString("body")
                        );

                        notification.setType(
                                resultSet.getString("type")
                        );

                        notification.setRelatedJobId(
                                resultSet.getObject(
                                        "related_job_id",
                                        Long.class
                                )
                        );

                        notification.setCreatedAt(
                                resultSet.getTimestamp("created_at")
                        );

                        notification.setAttemptCount(
                                resultSet.getInt("attempt_count")
                        );

                        notifications.add(notification);
                    }
                }
            }

            return null;
        });

        return notifications;
    }
    public void updateDeliveryStatus(
            Long notificationId,
            String status,
            String errorMessage) {

        jdbcTemplate.update(connection -> {

            CallableStatement statement =
                    connection.prepareCall(
                            "{call UPDATE_NOTIFICATION_DELIVERY(?, ?, ?)}"
                    );

            statement.setLong(1, notificationId);
            statement.setString(2, status);
            statement.setString(3, errorMessage);

            return statement;
        });
    }
}