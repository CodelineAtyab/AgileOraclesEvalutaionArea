package hrrecruitmentmanagementsystem.api.demo.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class SlackNotifierService {

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public SlackNotifierService(RestTemplate restTemplate,
                                @Value("${slack.webhook.url}") String webhookUrl) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
    }


    public void sendMessage(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> payload = Map.of("text", text);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(webhookUrl, request, String.class);
    }
}