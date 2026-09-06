package com.agileoracleseval.slitheringeval.alharithAlkindi2.RMS.src.main.java;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class SlackClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String webhookUrl;

    public SlackClient(@Value("${slack.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public boolean post(String subject, String body) {
        String text = escapeForJson(subject + "\n" + body);

        if (webhookUrl == null || webhookUrl.isBlank()) {
            System.out.println("[Slack stub - no webhook configured] " + subject + " | " + body);
            return true;
        }

        String payload = "{\"text\":\"" + text + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            }
            System.err.println("Slack post failed, status " + response.statusCode() + ": " + response.body());
            return false;
        } catch (Exception e) {
            System.err.println("Slack post threw an exception: " + e.getMessage());
            return false;
        }
    }

    private String escapeForJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}