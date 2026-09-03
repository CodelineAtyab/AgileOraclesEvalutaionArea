package com.vacancyloop.consumer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SlackService {

    private final RestClient restClient;
    private final String webhookUrl;

    public SlackService(
            @Value("${slack.webhook.url}") String webhookUrl
    ) {
        this.restClient = RestClient.create();
        this.webhookUrl = webhookUrl;
    }

    public void sendMessage(String message) {

        Map<String, String> payload = Map.of(
                "text", message
        );

        restClient.post()
                .uri(webhookUrl)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}