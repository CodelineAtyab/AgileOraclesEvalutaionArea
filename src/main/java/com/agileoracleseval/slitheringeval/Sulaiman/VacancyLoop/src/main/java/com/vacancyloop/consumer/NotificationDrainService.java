package com.vacancyloop.consumer;

import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;

/**
 * Steps 13-15, the Java half of the hand-over.
 *
 * The database opens the set (step 13): we call sp_get_pending_notifications,
 * which OPENs a SYS_REFCURSOR and hands it back live. We do NOT re-query the
 * notification table from Java — the caller is given the cursor, not a copy,
 * exactly as the brief requires.
 *
 * The consumer drains it (step 14): call, walk the set row by row, stop
 * cleanly when it ends.
 *
 * Slack gets the word (step 15): one record, one message, in order. After
 * each successful post we call sp_mark_notification_sent for that id.
 *
 * "What happens to a record already sent is your call — defend it":
 *   We mark each row sent immediately AFTER its Slack post succeeds, one at
 *   a time, not in a batch at the end. This is at-least-once delivery, not
 *   exactly-once: if the process dies (or the connection drops) in the
 *   narrow window between a successful Slack post and the matching
 *   sp_mark_notification_sent call, that row is still sent_flag='N' and
 *   will be posted again on the next run — a duplicate Slack message.
 *   We accept that risk deliberately: a Slack Incoming Webhook is cheap to
 *   repeat, and a duplicate alert is far less harmful than a lost one. What
 *   this design does guarantee is that nothing is ever lost — every row
 *   that hasn't been confirmed sent stays in the queue and gets retried,
 *   since sp_get_pending_notifications only returns sent_flag='N'.
 */
@Service
public class NotificationDrainService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDrainService.class);

    private final JdbcTemplate jdbcTemplate;
    private final SlackClient slackClient;

    // Credentials/config come from application.properties or the
    // environment — never hard-coded. (The Slack URL lives in SlackClient.)
    public NotificationDrainService(JdbcTemplate jdbcTemplate, SlackClient slackClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.slackClient = slackClient;
    }

    public void drainPendingNotifications() {
        log.info("Consumer started — draining pending notifications.");

        // execute() gives us the raw Connection so we can drive the
        // callable statement and its OUT ref cursor directly.
        Integer sent = jdbcTemplate.execute((Connection con) -> {
            int count = 0;

            try (CallableStatement cs =
                     con.prepareCall("{ call sp_get_pending_notifications(?) }")) {

                cs.registerOutParameter(1, OracleTypes.CURSOR);
                cs.execute();

                // Walk the handed-over set. try-with-resources guarantees the
                // cursor is closed even if a row misbehaves mid-drain.
                try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                    while (rs.next()) {
                        long   id      = rs.getLong("notification_id");
                        String subject = rs.getString("subject");
                        String body    = rs.getString("body");
                        Timestamp createdAt = rs.getTimestamp("created_at");

                        String message = "*" + subject + "*\n" + body;

                        // Post first...
                        slackClient.postMessage(message);

                        // ...then mark sent, so a crash can't lose the fact
                        // that this one was delivered.
                        markSent(con, id);

                        count++;
                        log.info("Delivered notification {} (created {}).", id, createdAt);
                    }
                }
            }
            return count;
        });

        log.info("Drain complete — {} notification(s) posted to Slack. Exiting.", sent);
    }

    /**
     * Confirms delivery of a single notification via the dedicated procedure,
     * keeping the "read the queue" (13) and "confirm delivery" boundaries
     * clean. Uses the same connection/transaction as the drain.
     */
    private void markSent(Connection con, long notificationId) throws java.sql.SQLException {
        try (CallableStatement cs =
                 con.prepareCall("{ call sp_mark_notification_sent(?) }")) {
            cs.setLong(1, notificationId);
            cs.execute();
        }
    }
}
