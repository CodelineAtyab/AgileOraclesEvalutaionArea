// Read pending notifications and mark successful deliveries

package notification_consumer;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificationRepository {

    private final DataSource dataSource;

    public NotificationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Notification> getPendingNotifications() throws Exception {
        List<Notification> notifications = new ArrayList<>();

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(
                                "{ call get_pending_notifications(?) }"
                        )
        ) {
            statement.registerOutParameter(1, Types.REF_CURSOR);
            statement.execute();

            try (ResultSet resultSet = (ResultSet) statement.getObject(1)) {
                while (resultSet.next()) {
                    notifications.add(
                            new Notification(
                                    resultSet.getLong("notification_id"),
                                    resultSet.getString("subject"),
                                    resultSet.getString("body"),
                                    resultSet.getTimestamp("created_at")
                                            .toLocalDateTime()
                            )
                    );
                }
            }
        }

        return notifications;
    }

    public void markNotificationSent(long notificationId) throws Exception {
        try (
                Connection connection = dataSource.getConnection();
                CallableStatement statement =
                        connection.prepareCall(
                                "{ call mark_notification_sent(?) }"
                        )
        ) {
            statement.setLong(1, notificationId);
            statement.execute();
        }
    }
}
