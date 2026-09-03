package com.vacancyloop.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VacancyLoopConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacancyLoopConsumerApplication.class, args);
    }

}
