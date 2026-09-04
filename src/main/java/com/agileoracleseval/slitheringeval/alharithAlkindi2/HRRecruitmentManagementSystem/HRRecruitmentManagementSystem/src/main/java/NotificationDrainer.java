package HRRecruitmentManagementSystem.HRRecruitmentManagementSystem;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Component
public class NotificationDrainer {

    private final DataSource dataSource;
    private final SlackClient slackClient;

    public NotificationDrainer(DataSource dataSource, SlackClient slackClient) {
        this.dataSource = dataSource;
        this.slackClient = slackClient;
    }

    public int drain() {
        int sentCount = 0;

        try (Connection connection = dataSource.getConnection();
             CallableStatement call = connection.prepareCall("{call get_pending_notifications(?)}")) {

            call.registerOutParameter(1, Types.REF_CURSOR);
            call.execute();

            try (ResultSet rs = (ResultSet) call.getObject(1)) {
                while (rs.next()) {
                    NotificationRecord record = new NotificationRecord(
                            rs.getLong("id"),
                            rs.getLong("job_id"),
                            rs.getString("subject"),
                            rs.getString("body"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at")
                    );

                    boolean posted = slackClient.post(record.subject(), record.body());

                    if (posted) {
                        markSent(connection, record.id());
                        sentCount++;
                    } else {
                        System.err.println("Leaving notification " + record.id() + " as PENDING after a failed Slack post.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed while draining pending notifications", e);
        }

        return sentCount;
    }

    private void markSent(Connection connection, long notificationId) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE notifications SET status = 'SENT' WHERE id = ?")) {
            update.setLong(1, notificationId);
            update.executeUpdate();
        }
    }
}