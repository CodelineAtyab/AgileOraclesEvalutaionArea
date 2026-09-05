package org.example.hrnotificationconsumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SlackClient {

    private final RestClient restClient;
    private final String webhookUrl;

    public SlackClient(
            @Value("${slack.webhook-url}") String webhookUrl
    ) {
        this.restClient = RestClient.create();
        this.webhookUrl = webhookUrl;
    }

    public void send(NotificationMessage notification) {
        String message =
                "*" + notification.subject() + "*\n"
                        + notification.body();

        restClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", message))
                .retrieve()
                .toBodilessEntity();
    }
}