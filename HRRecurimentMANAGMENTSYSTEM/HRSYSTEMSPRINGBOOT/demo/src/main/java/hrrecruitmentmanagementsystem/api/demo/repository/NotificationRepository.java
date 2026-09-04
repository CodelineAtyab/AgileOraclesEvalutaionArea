package hrrecruitmentmanagementsystem.api.demo.repository;

import hrrecruitmentmanagementsystem.api.demo.mpdel.NotificationRecord;
import oracle.jdbc.OracleTypes;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificationRepository {

    private final DataSource dataSource;

    public NotificationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public List<NotificationRecord> fetchPendingNotifications() {
        List<NotificationRecord> results = new ArrayList<>();

        String sql = "{call get_pending_notifications(?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, OracleTypes.CURSOR);
            stmt.execute();

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    long notificationId = rs.getLong("notification_id");
                    long jobIdRaw = rs.getLong("job_id");
                    Long jobId = rs.wasNull() ? null : jobIdRaw;
                    String subject = rs.getString("subject");
                    String body = rs.getString("body");

                    results.add(new NotificationRecord(notificationId, jobId, subject, body));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch pending notifications", e);
        }

        return results;
    }


    public void markAsSent(long notificationId) {
        String sql = "{call mark_notification_sent(?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setLong(1, notificationId);
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark notification " + notificationId + " as sent", e);
        }
    }
}