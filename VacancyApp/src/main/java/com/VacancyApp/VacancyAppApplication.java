package com.VacancyApp;

import com.VacancyApp.config.SlackProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SlackProperties.class)
public class VacancyAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VacancyAppApplication.class, args);
	}

}
