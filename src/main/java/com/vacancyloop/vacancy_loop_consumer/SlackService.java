package com.vacancyloop.vacancy_loop_consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

// sends one message to slack using the webhook url
@Service
public class SlackService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String text) {
        Map<String, String> payload = Map.of("text", text);
        restTemplate.postForObject(webhookUrl, payload, String.class);
    }
}
