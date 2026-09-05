package org.example.hrnotificationconsumer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Map;

@Repository
public class NotificationRepository {

    private final SimpleJdbcCall getPendingNotificationsCall;
    private final SimpleJdbcCall markNotificationSentCall;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.getPendingNotificationsCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withoutProcedureColumnMetaDataAccess()
                        .withProcedureName("GET_PENDING_NOTIFICATIONS")
                        .declareParameters(
                                new SqlOutParameter(
                                        "P_NOTIFICATION_CURSOR",
                                        Types.REF_CURSOR,
                                        (resultSet, rowNumber) ->
                                                new NotificationMessage(
                                                        resultSet.getLong("NOTIFICATION_ID"),
                                                        resultSet.getString("SUBJECT"),
                                                        resultSet.getString("BODY"),
                                                        resultSet.getTimestamp("CREATED_AT")
                                                                .toLocalDateTime()
                                                )
                                )
                        );

        this.markNotificationSentCall =
                new SimpleJdbcCall(jdbcTemplate)
                        .withoutProcedureColumnMetaDataAccess()
                        .withProcedureName("MARK_NOTIFICATION_SENT")
                        .declareParameters(
                                new SqlParameter(
                                        "P_NOTIFICATION_ID",
                                        Types.NUMERIC
                                )
                        );
    }

    @SuppressWarnings("unchecked")
    public List<NotificationMessage> findPendingNotifications() {
        Map<String, Object> result =
                getPendingNotificationsCall.execute();

        return (List<NotificationMessage>) result.getOrDefault(
                "P_NOTIFICATION_CURSOR",
                List.of()
        );
    }

    public void markAsSent(long notificationId) {
        markNotificationSentCall.execute(
                Map.of("P_NOTIFICATION_ID", notificationId)
        );
    }
}