package com.careflow.backend.modules.notification;

import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.repository.NotificationRepository;
import com.careflow.backend.modules.notification.service.NotificationDeliveryService;
import com.careflow.backend.modules.notification.service.NotificationProcessingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProcessingServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDeliveryService deliveryService;

    private NotificationProcessingService processingService;

    @BeforeEach
    void setUp() {
        processingService = new NotificationProcessingService(
            notificationRepository,
            deliveryService
        );
    }

    @Test
    void shouldMarkNotificationAsSentAfterSuccessfulDelivery() {
        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PROCESSING);

        when(notificationRepository.findById(notificationId))
            .thenReturn(Optional.of(notification));

        processingService.processClaimedNotification(notificationId);

        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertNotNull(notification.getSentAt());
        assertNull(notification.getErrorMessage());

        verify(deliveryService).deliver(notification);
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldMarkNotificationAsFailedWhenDeliveryThrowsException() {
        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PROCESSING);

        when(notificationRepository.findById(notificationId))
            .thenReturn(Optional.of(notification));

        doThrow(new RuntimeException("Email provider unavailable"))
            .when(deliveryService)
            .deliver(notification);

        processingService.processClaimedNotification(notificationId);

        assertEquals(NotificationStatus.FAILED, notification.getStatus());
        assertEquals(
            "Email provider unavailable",
            notification.getErrorMessage()
        );

        verify(deliveryService).deliver(notification);
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldIgnoreNotificationThatDoesNotExist() {
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId))
            .thenReturn(Optional.empty());

        processingService.processClaimedNotification(notificationId);

        verifyNoInteractions(deliveryService);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldIgnoreNotificationThatIsNotProcessing() {
        UUID notificationId = UUID.randomUUID();

        Notification notification = new Notification();
        notification.setStatus(NotificationStatus.PENDING);

        when(notificationRepository.findById(notificationId))
            .thenReturn(Optional.of(notification));

        processingService.processClaimedNotification(notificationId);

        verifyNoInteractions(deliveryService);
        verify(notificationRepository, never()).save(any());
    }
}