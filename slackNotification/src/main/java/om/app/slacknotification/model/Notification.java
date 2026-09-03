package om.app.slacknotification.model;

    public class Notification {

        private Long notificationId;
        private String subject;
        private String body;
        private String status;

        public Notification(Long notificationId,
                            String subject,
                            String body,
                            String status) {
            this.notificationId = notificationId;
            this.subject = subject;
            this.body = body;
            this.status = status;
        }

        public Long getNotificationId() {
            return notificationId;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getStatus() {
            return status;
        }
    }

