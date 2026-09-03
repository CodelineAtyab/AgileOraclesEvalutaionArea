package com.hrrecruitment.notifications;

import java.util.List;

public interface NotificationRepository {
    List<Notification> findPendingNotifications();

    void markSent(long notificationId);

    void recordFailure(long notificationId);
}
