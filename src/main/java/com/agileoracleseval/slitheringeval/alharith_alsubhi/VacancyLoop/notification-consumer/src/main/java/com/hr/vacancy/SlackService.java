package com.hr.vacancy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SlackService {

    private final RestTemplate rest = new RestTemplate();

    @Value("${slack.webhook-url}")
    private String webhookUrl;

    public void send(NotificationRow row) {
        String text = "*" + row.getSubject() + "*\n"
                + row.getBody()
                + "\n_source=" + row.getSourceType()
                + ", id=" + row.getSourceId() + "_";

        // no real webhook yet -> print only
        if (webhookUrl == null
                || webhookUrl.contains("YOUR/WEBHOOK")
                || webhookUrl.contains("XXX/YYY")) {
            System.out.println("--- slack (console mode) ---");
            System.out.println(text);
            System.out.println("----------------------------");
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("text", text);
        rest.postForEntity(webhookUrl, payload, String.class);
    }
}
