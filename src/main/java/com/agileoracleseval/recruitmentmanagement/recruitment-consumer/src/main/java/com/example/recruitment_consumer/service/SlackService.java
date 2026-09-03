package com.example.recruitment_consumer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SlackService {

    private final RestClient restClient;
    private final String webhookUrl;

    public SlackService(
            @Value("${slack.webhook-url}") String webhookUrl
    ) {
        this.restClient = RestClient.create();
        this.webhookUrl = webhookUrl;
    }

    public boolean sendMessage(String subject, String body) {

        String message = "*" + subject + "*\n" + body;

        Map<String, String> requestBody = Map.of("text", message);

        try {
            restClient
                    .post()
                    .uri(webhookUrl)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (Exception e) {
            // Exception messages can contain the webhook URL, so only print the type.
            System.out.println("Slack webhook request failed: "
                    + e.getClass().getSimpleName());

            return false;
        }
    }
}
