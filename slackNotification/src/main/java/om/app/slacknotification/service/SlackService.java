package om.app.slacknotification.service;

import om.app.slacknotification.model.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class SlackService {

    private final RestClient restClient = RestClient.create();

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    public void sendNotification(Notification notification) {

        String message =
                "*" + notification.getSubject() + "*\n"
                        + notification.getBody();

        restClient.post()
                .uri(slackWebhookUrl)
                .body(Map.of("text", message))
                .retrieve()
                .toBodilessEntity();
    }
}