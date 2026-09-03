package com.vacancyloop.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * Step 14 — the consumer entry point.
 *
 * One-shot by design: Spring Boot starts, the CommandLineRunner drains the
 * queue exactly once, and the application exits. This matches the brief's
 * "done means": started once, drains the queue into Slack, exits without
 * error. No web server, no scheduler here — lanes A-D run entirely in the
 * database and keep working whether or not this process is ever started.
 */
@SpringBootApplication
public class VacancyLoopConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(VacancyLoopConsumerApplication.class);

    public static void main(String[] args) {
        // Not a web app — disable the web server so it exits when the
        // runner finishes instead of staying up.
        SpringApplication app = new SpringApplication(VacancyLoopConsumerApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);

        // Own the exit code explicitly rather than letting a runner failure
        // propagate as an uncaught exception: SpringApplication already logs
        // the full stack trace for a failed run, so here we just record the
        // one-line outcome and pick the exit status ourselves.
        try {
            int exitCode = SpringApplication.exit(app.run(args));
            System.exit(exitCode);
        } catch (Exception ex) {
            log.error("Notification drain failed — exiting with status 1: {}", ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Drives the drain-once-and-stop flow. Returns normally on success so
     * the JVM exit code is 0.
     */
    @Bean
    @Order(1)
    CommandLineRunner runOnce(NotificationDrainService drainService) {
        return args -> drainService.drainPendingNotifications();
    }
}
