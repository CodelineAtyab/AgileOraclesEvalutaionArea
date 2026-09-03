package com.vacancy.consumer;

import oracle.jdbc.OracleTypes;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class NotificationConsumer {

    private static final String GET_PENDING_NOTIFICATIONS = "{ call GET_PENDING_NOTIFICATIONS(?) }";

    private static final String MARK_SENT_SQL = """
            UPDATE NOTIFICATIONS
               SET status = 'SENT',
                   sent_at = SYSTIMESTAMP
             WHERE notification_id = ?
            """;

    private final DataSource dataSource;
    private final SlackService slackService;

    public NotificationConsumer(DataSource dataSource, SlackService slackService) {
        this.dataSource = dataSource;
        this.slackService = slackService;
    }

    public void processPendingNotifications() throws SQLException {
        // DataSource gives the app a database connection using application.properties.
        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(GET_PENDING_NOTIFICATIONS)) {

            // The PL/SQL procedure returns a SYS_REFCURSOR in its first parameter.
            statement.registerOutParameter(1, OracleTypes.CURSOR);
            statement.execute();

            try (ResultSet notifications = (ResultSet) statement.getObject(1)) {
                // Reading and sending inside this loop keeps the database order unchanged.
                while (notifications.next() && !Thread.currentThread().isInterrupted()) {
                    processNotificationRow(connection, notifications);
                }
            }
        }
    }

    private void processNotificationRow(Connection connection, ResultSet notifications) throws SQLException {
        long notificationId = notifications.getLong("notification_id");
        Object relatedJobId = notifications.getObject("related_job_id");
        String subject = notifications.getString("subject");
        String body = notifications.getString("body");
        Object createdAt = notifications.getObject("created_at");
        String status = notifications.getString("status");
        Object sentAt = notifications.getObject("sent_at");

        System.out.println("Sending notification " + notificationId
                + " (job=" + relatedJobId
                + ", status=" + status
                + ", created_at=" + createdAt
                + ", sent_at=" + sentAt + ")");

        try {
            String slackTitle = buildSlackTitle(subject);
            String slackBody = buildSlackBody(notificationId, relatedJobId, subject, body, status, createdAt);

            slackService.sendMessage(slackTitle, slackBody);
            markNotificationAsSent(connection, notificationId);
            System.out.println("Notification " + notificationId + " marked as SENT.");
        } catch (IOException exception) {
            // If Slack fails, the database row is not updated, so it can be retried later.
            System.err.println("Failed to send notification " + notificationId + ": " + exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Sending was interrupted for notification " + notificationId + ".");
        }
    }

    private String buildSlackTitle(String subject) {
        if (isDailyJobApplicationsDigest(subject)) {
            return "📋 Daily Job Applications Digest";
        }

        return "📢 New Vacancy Notification";
    }

    private String buildSlackBody(long notificationId, Object relatedJobId, String subject, String body,
                                  String status, Object createdAt) {
        if (isDailyJobApplicationsDigest(subject)) {
            return "Notification ID: " + notificationId + "\n\n"
                    + nullToEmpty(body) + "\n\n"
                    + "Status: " + nullToEmpty(status) + "\n"
                    + "Created At: " + valueOrNotAvailable(createdAt);
        }

        return "Notification ID: " + notificationId + "\n"
                + "Job ID: " + valueOrNotAvailable(relatedJobId) + "\n"
                + "Subject: " + nullToEmpty(subject) + "\n\n"
                + "Message:\n"
                + nullToEmpty(body) + "\n\n"
                + "Status: " + nullToEmpty(status) + "\n"
                + "Created At: " + valueOrNotAvailable(createdAt);
    }

    private boolean isDailyJobApplicationsDigest(String subject) {
        return "Daily Job Applications Digest".equalsIgnoreCase(nullToEmpty(subject).trim());
    }

    private String valueOrNotAvailable(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void markNotificationAsSent(Connection connection, long notificationId) throws SQLException {
        // This update prevents a successfully sent notification from being sent again.
        try (PreparedStatement statement = connection.prepareStatement(MARK_SENT_SQL)) {
            statement.setLong(1, notificationId);
            statement.executeUpdate();
        }
    }
}
