package com.example.recruitment_consumer.model;

public class Notification {

    private long id;
    private String subject;
    private String body;

    public Notification(long id, String subject, String body) {
        this.id = id;
        this.subject = subject;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}

