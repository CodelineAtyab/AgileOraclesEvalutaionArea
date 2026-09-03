package com.vacancy.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class SlackService {

    private final HttpClient httpClient;
    private final String webhookUrl;

    public SlackService(@Value("${slack.webhook.url}") String webhookUrl) {
        // The webhook URL comes from SLACK_WEBHOOK_URL, not from hard-coded Java code.
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendMessage(String subject, String body) throws IOException, InterruptedException {
        // Slack incoming webhooks expect a JSON body with a text field.
        String text = nullToEmpty(subject) + "\n\n" + nullToEmpty(body);

        // String text = "[Al-Jolanda App]\n\n" + nullToEmpty(subject) + "\n\n" + nullToEmpty(body);

        String payload = "{\"text\":\"" + escapeJson(text) + "\"}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Slack returned HTTP " + response.statusCode());
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);

            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }

        return escaped.toString();
    }
}
