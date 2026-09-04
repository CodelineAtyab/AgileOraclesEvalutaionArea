package hrrecruitmentmanagementsystem.api.demo.mpdel;


public class NotificationRecord {

    private final long notificationId;
    private final Long jobId;
    private final String subject;
    private final String body;

    public NotificationRecord(long notificationId, Long jobId, String subject, String body) {
        this.notificationId = notificationId;
        this.jobId = jobId;
        this.subject = subject;
        this.body = body;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }
}