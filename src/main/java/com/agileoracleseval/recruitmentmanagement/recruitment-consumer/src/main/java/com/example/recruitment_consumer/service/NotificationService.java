package com.example.recruitment_consumer.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;

@Service
public class NotificationService {

    private final JdbcTemplate jdbcTemplate;
    private final SlackService slackService;

    public NotificationService(
            JdbcTemplate jdbcTemplate,
            SlackService slackService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.slackService = slackService;
    }

    public void consumePendingNotifications() {
        jdbcTemplate.execute(
                (Connection connection) -> {
                    int pendingRowsRead = 0;
                    int slackPostsSucceeded = 0;
                    int notificationsMarkedSent = 0;
                    int notificationsLeftPending = 0;

                    try (CallableStatement statement = connection.prepareCall(
                            "{call GET_PENDING_NOTIFICATIONS(?)}"
                    )) {
                        statement.registerOutParameter(1, Types.REF_CURSOR);
                        statement.execute();

                        try (ResultSet resultSet =
                                     (ResultSet) statement.getObject(1)) {
                            while (resultSet.next()) {
                                pendingRowsRead++;
                                long id = -1;

                                try {
                                    id = resultSet.getLong("notification_id");
                                    String subject = resultSet.getString("subject");
                                    String body = resultSet.getString("body");
                                    String status = resultSet.getString("status");

                                    System.out.println(
                                            "Processing notification ID: " + id
                                    );
                                    System.out.println("Current status: " + status);

                                    boolean slackSent =
                                            slackService.sendMessage(subject, body);

                                    if (slackSent) {
                                        slackPostsSucceeded++;
                                        markAsSent(id);
                                        notificationsMarkedSent++;

                                        System.out.println(
                                                "Notification " + id
                                                        + " sent successfully."
                                        );
                                    } else {
                                        notificationsLeftPending++;

                                        System.out.println(
                                                "Notification " + id
                                                        + " failed and remains PENDING."
                                        );
                                    }
                                } catch (Exception e) {
                                    notificationsLeftPending++;

                                    if (id >= 0) {
                                        System.out.println(
                                                "Notification " + id
                                                        + " failed and remains PENDING."
                                        );
                                    } else {
                                        System.out.println(
                                                "A notification failed and remains PENDING."
                                        );
                                    }

                                    // Print only the exception type to avoid exposing secrets.
                                    System.out.println(
                                            "Error type: "
                                                    + e.getClass().getSimpleName()
                                    );
                                }
                            }
                        }
                    }

                    System.out.println("Pending rows read: " + pendingRowsRead);
                    System.out.println(
                            "Successfully posted to Slack: "
                                    + slackPostsSucceeded
                    );
                    System.out.println(
                            "Marked SENT: " + notificationsMarkedSent
                    );
                    System.out.println(
                            "Left PENDING: " + notificationsLeftPending
                    );

                    return null;
                }
        );
    }

    private void markAsSent(long notificationId) {
        jdbcTemplate.execute(
                (Connection connection) -> {
                    try (CallableStatement statement = connection.prepareCall(
                            "{call MARK_NOTIFICATION_SENT(?)}"
                    )) {
                        statement.setLong(1, notificationId);
                        statement.execute();
                    }

                    return null;
                }
        );
    }
}
