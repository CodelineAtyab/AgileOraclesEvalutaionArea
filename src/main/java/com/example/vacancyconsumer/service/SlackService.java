package com.example.vacancyconsumer.service;

import com.example.vacancyconsumer.model.NotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class SlackService {

    private final HttpClient httpClient;
    private final boolean enabled;
    private final String webhookUrl;

    public SlackService(
            @Value("${slack.enabled:false}") boolean enabled,
            @Value("${slack.webhook-url:}") String webhookUrl
    ) {
        this.httpClient = HttpClient.newHttpClient();
        this.enabled = enabled;
        this.webhookUrl = webhookUrl;
    }

    /**
     * Sends one database notification as one Slack Incoming Webhook message.
     *
     * When slack.enabled=false, this method performs a dry run: it prints the
     * message and returns false so the notification stays PENDING. This lets
     * Oracle -> Java be tested safely before enabling the real webhook.
     */
    public boolean sendNotification(NotificationMessage notification) {

        String message = notification.subject() + "\n" + notification.body();

        if (!enabled) {
            System.out.println("[DRY RUN] Slack is disabled. Message would be:");
            System.out.println(message);
            return false;
        }

        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.err.println(
                    "Slack is enabled, but SLACK_WEBHOOK_URL is not configured."
            );
            return false;
        }

        try {
            String jsonBody =
                    "{\"text\":\"" + escapeJson(message) + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(
                            HttpRequest.BodyPublishers.ofString(
                                    jsonBody,
                                    StandardCharsets.UTF_8
                            )
                    )
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            boolean success =
                    response.statusCode() >= 200
                            && response.statusCode() < 300;

            if (!success) {
                System.err.println(
                        "Slack webhook failed for notification "
                                + notification.id()
                                + ". HTTP "
                                + response.statusCode()
                                + ", response: "
                                + response.body()
                );
            }

            return success;
        } catch (Exception exception) {
            System.err.println(
                    "Slack delivery failed for notification "
                            + notification.id()
                            + ": "
                            + exception.getMessage()
            );
            return false;
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
