package om.app.slacknotification.service;

import om.app.slacknotification.model.Notification;
import oracle.jdbc.OracleTypes;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

@Service
public class NotificationConsumerService implements CommandLineRunner {

    private final DataSource dataSource;
    private final SlackService slackService;

    public NotificationConsumerService(
            DataSource dataSource,
            SlackService slackService) {

        this.dataSource = dataSource;
        this.slackService = slackService;
    }

    @Override
    public void run(String... args) throws Exception {
        consumePendingNotifications();
    }

    public void consumePendingNotifications() throws Exception {

        try (
                Connection connection = dataSource.getConnection();

                CallableStatement statement =
                        connection.prepareCall(
                                "{call get_pending_notifications(?)}"
                        )
        ) {

            // The procedure returns a SYS_REFCURSOR
            statement.registerOutParameter(
                    1,
                    OracleTypes.CURSOR
            );

            statement.execute();

            try (
                    ResultSet resultSet =
                            (ResultSet) statement.getObject(1)
            ) {

                while (resultSet.next()) {

                    // Create Java object from Oracle row
                    Notification notification =
                            new Notification(
                                    resultSet.getLong("notification_id"),
                                    resultSet.getString("subject"),
                                    resultSet.getString("body"),
                                    resultSet.getString("status")
                            );

                    // Send notification to Slack
                    slackService.sendNotification(notification);

                    // Change PENDING to SENT
                    markAsSent(notification.getNotificationId());
                }
            }
        }
    }

    private void markAsSent(Long notificationId) throws Exception {

        String sql =
                "UPDATE notifications " +
                        "SET status = 'SENT', sent_at = SYSTIMESTAMP " +
                        "WHERE notification_id = ? " +
                        "AND status = 'PENDING'";

        try (
                Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(sql)
        ) {

            statement.setLong(1, notificationId);

            statement.executeUpdate();
        }
    }
}