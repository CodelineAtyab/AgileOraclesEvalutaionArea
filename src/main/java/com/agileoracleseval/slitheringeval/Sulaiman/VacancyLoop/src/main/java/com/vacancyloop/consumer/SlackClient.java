package com.vacancyloop.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Step 15 — posts one message to a Slack Incoming Webhook.
 *
 * The webhook URL is a credential and is read from configuration
 * (slack.webhook-url), which in turn is bound from the SLACK_WEBHOOK_URL
 * environment variable in application.properties. It is never written into
 * source. An Incoming Webhook expects a JSON body of the form
 * {"text": "..."} and returns HTTP 200 with the literal body "ok".
 */
@Component
public class SlackClient {

    private final RestClient restClient;
    private final String webhookUrl;

    public SlackClient(@Value("${slack.webhook-url}") String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            throw new IllegalStateException(
                "slack.webhook-url is not configured. Set the SLACK_WEBHOOK_URL "
                + "environment variable — do not hard-code the token.");
        }
        this.webhookUrl = webhookUrl;
        this.restClient = RestClient.create();
    }

    /**
     * Posts a single message. Throws on a non-2xx response so the drain
     * loop does NOT mark the row sent — an undelivered message stays
     * pending and is retried on the next run.
     */
    public void postMessage(String text) {
        restClient.post()
            .uri(webhookUrl)
            .body(Map.of("text", text))
            .retrieve()
            .toBodilessEntity();   // 4xx/5xx throws RestClientResponseException
    }
}
