package com.vacancyloop.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

// Step 14: this is the consumer. It runs once, grabs whatever
// notifications are waiting in the outbox, sends them to Slack, then
// shuts itself down. It doesn't decide anything on its own - the
// database already figured out what's pending and what the message
// says, this class just picks it up and passes it along. That's the
// rule: Java isn't allowed to own any of lanes A-D.
@SpringBootApplication
public class VacancyLoopConsumerApplication implements CommandLineRunner {

    private final DataSource dataSource;
    private final NotificationSlackService notificationSlackService;

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    public VacancyLoopConsumerApplication(DataSource dataSource,
                                           NotificationSlackService notificationSlackService) {
        this.dataSource = dataSource;
        this.notificationSlackService = notificationSlackService;
    }

    public static void main(String[] args) {
        // using SpringApplication.exit + System.exit so the app actually
        // closes when it's done, instead of hanging around like a web server
        int exitCode = SpringApplication.exit(SpringApplication.run(VacancyLoopConsumerApplication.class, args));
        System.exit(exitCode);
    }

    @Override
    public void run(String... args) throws Exception {
        int drained = notificationSlackService.drainAndPost(dataSource, slackWebhookUrl);
        System.out.println("[vacancy-loop-consumer] done - drained " + drained + " notification(s).");
    }
}
