package com.careflow.backend.modules.notification;

import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.repository.NotificationRepository;
import com.careflow.backend.modules.notification.service.NotificationClaimService;
import com.careflow.backend.modules.notification.service.NotificationProcessingService;
import com.careflow.backend.modules.notification.service.NotificationWorker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationWorkerTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationClaimService claimService;

    @Mock
    private NotificationProcessingService processingService;

    private NotificationWorker worker;

    @BeforeEach
    void setUp() {
        worker = new NotificationWorker(
            notificationRepository,
            claimService,
            processingService
        );
    }

    @Test
    void shouldProcessNotificationWhenClaimSucceeds() {
        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);
        when(notification.getId()).thenReturn(notificationId);

        when(notificationRepository.findAllByStatusAndScheduledAtLessThanEqual(
            eq(NotificationStatus.PENDING),
            any(LocalDateTime.class)
        )).thenReturn(List.of(notification));

        when(claimService.claim(notificationId)).thenReturn(true);

        worker.processDueNotifications();

        verify(claimService).claim(notificationId);
        verify(processingService).processClaimedNotification(notificationId);
    }

    @Test
    void shouldSkipNotificationWhenClaimFails() {
        UUID notificationId = UUID.randomUUID();

        Notification notification = mock(Notification.class);
        when(notification.getId()).thenReturn(notificationId);

        when(notificationRepository.findAllByStatusAndScheduledAtLessThanEqual(
            eq(NotificationStatus.PENDING),
            any(LocalDateTime.class)
        )).thenReturn(List.of(notification));

        when(claimService.claim(notificationId)).thenReturn(false);

        worker.processDueNotifications();

        verify(claimService).claim(notificationId);
        verify(processingService, never())
            .processClaimedNotification(any(UUID.class));
    }

    @Test
    void shouldDoNothingWhenNoNotificationsAreDue() {
        when(notificationRepository.findAllByStatusAndScheduledAtLessThanEqual(
            eq(NotificationStatus.PENDING),
            any(LocalDateTime.class)
        )).thenReturn(List.of());

        worker.processDueNotifications();

        verifyNoInteractions(claimService, processingService);
    }
}