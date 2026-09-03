package com.hrrecruitment.notifications;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OracleNotificationRepository implements NotificationRepository {
    private final JdbcTemplate jdbcTemplate;

    public OracleNotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Notification> findPendingNotifications() {
        return jdbcTemplate.execute((CallableStatementCreator) connection -> {
            var statement = connection.prepareCall("{ call GET_PENDING_NOTIFICATIONS(?) }");
            statement.registerOutParameter(1, Types.REF_CURSOR);
            return statement;
        }, (CallableStatementCallback<List<Notification>>) statement -> {
            statement.execute();
            List<Notification> notifications = new ArrayList<>();

            // Oracle returns SYS_REFCURSOR as a JDBC ResultSet.
            try (ResultSet cursor = (ResultSet) statement.getObject(1)) {
                while (cursor.next()) {
                    notifications.add(new Notification(
                            cursor.getLong("NOTIFICATION_ID"),
                            cursor.getString("NOTIFICATION_TYPE"),
                            cursor.getString("SUBJECT"),
                            cursor.getString("BODY")));
                }
            }
            return notifications;
        });
    }

    @Override
    public void markSent(long notificationId) {
        jdbcTemplate.update("{ call MARK_NOTIFICATION_SENT(?) }", notificationId);
    }

    @Override
    public void recordFailure(long notificationId) {
        jdbcTemplate.update("{ call RECORD_NOTIFICATION_FAILURE(?) }", notificationId);
    }
}
