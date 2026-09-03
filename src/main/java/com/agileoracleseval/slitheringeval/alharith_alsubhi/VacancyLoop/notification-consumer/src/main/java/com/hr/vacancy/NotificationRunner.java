package com.hr.vacancy;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// runs once: pull pending, send, mark sent, then app stops
@Component
public class NotificationRunner implements CommandLineRunner {

    private final NotificationDao dao;
    private final SlackService slack;

    public NotificationRunner(NotificationDao dao, SlackService slack) {
        this.dao = dao;
        this.slack = slack;
    }

    @Override
    public void run(String... args) {
        System.out.println("starting notification consumer...");

        List<NotificationRow> pending = dao.fetchPending();
        System.out.println("pending count = " + pending.size());

        for (NotificationRow row : pending) {
            try {
                slack.send(row);
                dao.markSent(row.getId());
                System.out.println("sent id=" + row.getId());
            } catch (Exception e) {
                // leave PENDING so next run can retry
                System.out.println("failed id=" + row.getId() + " : " + e.getMessage());
            }
        }

        System.out.println("done");
    }
}
