package com.hrrecruitment.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HrRecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrRecruitmentApplication.class, args);
    }
}
