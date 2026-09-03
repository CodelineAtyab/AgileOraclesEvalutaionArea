package com.vacancyloop.consumer;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;

// Step 13-15: this calls get_pending_notifications, which opens a cursor
// over whatever's still pending and marks it delivered at the same time
// (see sql/06_handover.sql for why it's done that way). All this class
// does is walk that cursor and send each row to Slack, in order, until
// it's empty - it doesn't decide what's pending or what the message
// looks like, that's already handled on the database side.
@Service
public class NotificationSlackService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Opens the cursor, goes through every row once, posts each one to
    // Slack, and returns how many actually made it through.
    // Everything is in try-with-resources so the cursor/connection close
    // properly no matter what happens partway through the loop.
    // If sending ONE notification fails (network issue, Slack returns an
    // error, whatever) I just log it and keep going with the rest - one
    // bad row shouldn't kill the whole run. That row was already marked
    // delivered on the database side though, so if it fails here it's
    // genuinely lost, not something we retry - that's the trade-off from
    // step 13.
    public int drainAndPost(DataSource dataSource, String slackWebhookUrl) throws Exception {
        int succeeded = 0;
        int failed = 0;

        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall("{ call get_pending_notifications(?) }")) {

            statement.registerOutParameter(1, Types.REF_CURSOR);
            statement.execute();

            try (ResultSet resultSet = (ResultSet) statement.getObject(1)) {
                while (resultSet.next()) {
                    long notificationId = resultSet.getLong("notification_id");
                    String category = resultSet.getString("msg_category");
                    String subject = resultSet.getString("msg_subject");
                    String body = resultSet.getString("msg_body");

                    try {
                        postToSlack(slackWebhookUrl, category, subject, body);
                        System.out.println("[vacancy-loop-consumer] posted notification #" + notificationId
                                + " (" + category + ") to Slack");
                        succeeded++;
                    } catch (Exception rowFailure) {
                        System.out.println("[vacancy-loop-consumer] FAILED to post notification #" + notificationId
                                + " (" + category + "): " + rowFailure.getMessage()
                                + " - continuing with the rest of the queue");
                        failed++;
                    }
                }
            }
        }

        System.out.println("[vacancy-loop-consumer] drain summary: " + succeeded + " succeeded, "
                + failed + " failed, " + (succeeded + failed) + " total.");
        return succeeded;
    }

    // builds the Slack message and posts it. if there's no real webhook
    // set up yet it just prints what it would have sent instead of
    // crashing, so the app still runs fine before Slack is wired up
    private void postToSlack(String webhookUrl, String category, String subject, String body) throws Exception {
        if (webhookUrl == null || webhookUrl.isBlank() || webhookUrl.startsWith("REPLACE_WITH")) {
            System.out.println("[vacancy-loop-consumer] no Slack webhook configured yet - "
                    + "would have sent: [" + category + "] " + subject);
            return;
        }

        String text = ("*" + subject + "*\n" + body).replace("\"", "\\\"").replace("\n", "\\n");
        String payload = "{\"text\":\"" + text + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Slack webhook returned HTTP " + response.statusCode()
                    + ": " + response.body());
        }
    }
}
