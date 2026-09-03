package com.vacancyloop.vacancy_loop_consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

// calls the database's explicit cursor, walks the result set, sends each to slack
@Service
public class NotificationService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SlackService slackService;

    public void drainPendingNotifications() throws Exception {

        try (Connection conn = dataSource.getConnection()) {

            // step 1: ask the database to open the cursor with pending notifications
            CallableStatement getCursor = conn.prepareCall("{call get_pending_notifications(?)}");
            getCursor.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            getCursor.execute();

            ResultSet rs = (ResultSet) getCursor.getObject(1);

            // step 2: walk the result set one row at a time, exactly like an explicit cursor should be drained
            while (rs.next()) {
                long id = rs.getLong("notification_id");
                String subject = rs.getString("subject");
                String body = rs.getString("body");

                String slackText = "*" + subject + "*\n" + body;

                // step 3: send this one record, then mark it sent immediately
                slackService.sendMessage(slackText);
                markAsSent(conn, id);
            }

            rs.close();
            getCursor.close();
        }
    }

    // calls mark_notification_sent for a single notification right after it goes out
    private void markAsSent(Connection conn, long notificationId) throws Exception {
        CallableStatement markSent = conn.prepareCall("{call mark_notification_sent(?)}");
        markSent.setLong(1, notificationId);
        markSent.execute();
        markSent.close();
    }
}