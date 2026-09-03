package com.vacancyloop.consumer.model;

import java.sql.Timestamp;

public class NotificationMessage {

    private Long notificationId;
    private String subject;
    private String body;
    private String type;
    private Long relatedJobId;
    private Timestamp createdAt;
    private Integer attemptCount;

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getRelatedJobId() {
        return relatedJobId;
    }

    public void setRelatedJobId(Long relatedJobId) {
        this.relatedJobId = relatedJobId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }
}
