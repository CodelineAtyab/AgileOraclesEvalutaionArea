package com.hrrecruitment.notifications;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

class NotificationServiceTest {
    private final NotificationRepository repository = mock(NotificationRepository.class);
    private final SlackNotificationSender slackSender = mock(SlackNotificationSender.class);
    private final NotificationService service = new NotificationService(repository, slackSender);
    private final Notification notification = new Notification(8L, "INTERVIEW", "Interview scheduled", "Candidate interview is tomorrow");

    @Test
    void marksNotificationSentAfterSuccessfulSlackDelivery() throws Exception {
        when(repository.findPendingNotifications()).thenReturn(List.of(notification));

        service.processPendingNotifications();

        verify(slackSender).send(notification);
        verify(repository).markSent(8L);
        verify(repository, never()).recordFailure(anyLong());
    }

    @Test
    void recordsFailureAndDoesNotMarkSentWhenSlackFails() throws Exception {
        when(repository.findPendingNotifications()).thenReturn(List.of(notification));
        doThrow(new IOException("Slack unavailable")).when(slackSender).send(notification);

        service.processPendingNotifications();

        verify(repository).recordFailure(8L);
        verify(repository, never()).markSent(anyLong());
    }
}
