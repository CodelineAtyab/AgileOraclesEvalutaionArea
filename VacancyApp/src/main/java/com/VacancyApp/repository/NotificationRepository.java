package com.VacancyApp.repository;

import com.VacancyApp.model.Notification;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Calls the notification stored procedures (get pending, mark sent, mark failed). */
@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    static final RowMapper<Notification> NOTIFICATION_MAPPER = (rs, i) -> new Notification(
            rs.getLong("NOTIFICATION_ID"),
            rs.getString("SUBJECT"),
            rs.getString("BODY"),
            toLocalDateTime(rs, "CREATED_AT"),
            rs.getString("STATUS"),
            toLocalDateTime(rs, "SENT_AT")
    );

    /** Calls GET_PENDING_NOTIFICATIONS (one SYS_REFCURSOR OUT). */
    @SuppressWarnings("unchecked")
    public List<Notification> findPending() {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GET_PENDING_NOTIFICATIONS")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlOutParameter("P_NOTIFICATIONS", OracleTypes.CURSOR, NOTIFICATION_MAPPER));
        Map<String, Object> out = call.execute();
        return (List<Notification>) out.get("P_NOTIFICATIONS");
    }

    /** Calls MARK_NOTIFICATION_SENT; Oracle records sent_at. */
    public void markSent(long notificationId) {
        new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MARK_NOTIFICATION_SENT")
                .execute(Map.of("P_NOTIFICATION_ID", notificationId));
    }

    /** Calls MARK_NOTIFICATION_FAILED. */
    public void markFailed(long notificationId) {
        new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("MARK_NOTIFICATION_FAILED")
                .execute(Map.of("P_NOTIFICATION_ID", notificationId));
    }

    private static LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
