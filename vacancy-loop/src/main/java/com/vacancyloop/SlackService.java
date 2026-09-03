package com.vacancyloop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SlackService {

    private final String webhookUrl;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    public SlackService(
            @Value("${slack.webhook.url}") String webhookUrl
    ) {
        this.webhookUrl = webhookUrl;
    }


    public boolean sendMessage(String subject, String body)
            throws Exception {

        String message =
                subject + "\n" + body;

        String json =
                "{\"text\":\"" + escapeJson(message) + "\"}";


        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
                        )
                        .build();


        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );


        return response.statusCode() == 200;
    }


    private String escapeJson(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
