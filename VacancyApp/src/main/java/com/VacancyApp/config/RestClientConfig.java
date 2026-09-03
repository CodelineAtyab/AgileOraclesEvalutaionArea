package com.VacancyApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Provides the RestClient used to POST messages to the Slack webhook. */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient slackRestClient() {
        return RestClient.builder().build();
    }
}
