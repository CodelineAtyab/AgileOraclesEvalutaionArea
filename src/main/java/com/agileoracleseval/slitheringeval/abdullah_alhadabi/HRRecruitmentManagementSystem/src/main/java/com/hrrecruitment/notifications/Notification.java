package com.hrrecruitment.notifications;

/** A notification returned by the existing Oracle notification procedures. */
public class Notification {
    private final long notificationId;
    private final String notificationType;
    private final String subject;
    private final String body;

    public Notification(long notificationId, String notificationType, String subject, String body) {
        this.notificationId = notificationId;
        this.notificationType = notificationType;
        this.subject = subject;
        this.body = body;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}
