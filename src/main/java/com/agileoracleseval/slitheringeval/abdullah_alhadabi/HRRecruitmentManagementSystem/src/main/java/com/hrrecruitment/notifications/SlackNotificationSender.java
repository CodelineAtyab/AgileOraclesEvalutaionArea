package com.hrrecruitment.notifications;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class SlackNotificationSender {
    private final String webhookUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Spring supplies this value from slack.webhook-url, which is populated by
     * the SLACK_WEBHOOK_URL environment variable.
     */
    public SlackNotificationSender(@Value("${slack.webhook-url}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void send(Notification notification) throws IOException, InterruptedException {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalStateException("SLACK_WEBHOOK_URL must be configured");
        }

        String text = "[" + notification.getNotificationType() + "]\n"
                + "Subject: " + notification.getSubject() + "\n\n"
                + notification.getBody();
        HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + escapeJson(text) + "\"}"))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Slack returned HTTP " + response.statusCode());
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
