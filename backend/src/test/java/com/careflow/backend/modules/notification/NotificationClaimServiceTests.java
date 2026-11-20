package com.careflow.backend.modules.notification;

import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.repository.NotificationRepository;
import com.careflow.backend.modules.notification.service.NotificationClaimService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationClaimServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new NotificationClaimService(notificationRepository);
    }

    @Test
    void shouldClaimNotificationWhenItIsPendingAndDue() {
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.claimIfPending(
            eq(notificationId),
            any(),
            eq(NotificationStatus.PENDING),
            eq(NotificationStatus.PROCESSING)
        )).thenReturn(1);

        boolean claimed = claimService.claim(notificationId);

        assertTrue(claimed);

        verify(notificationRepository).claimIfPending(
            eq(notificationId),
            any(),
            eq(NotificationStatus.PENDING),
            eq(NotificationStatus.PROCESSING)
        );
    }

    @Test
    void shouldNotClaimNotificationWhenConditionalUpdateAffectsNoRows() {
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.claimIfPending(
            eq(notificationId),
            any(),
            eq(NotificationStatus.PENDING),
            eq(NotificationStatus.PROCESSING)
        )).thenReturn(0);

        boolean claimed = claimService.claim(notificationId);

        assertFalse(claimed);

        verify(notificationRepository).claimIfPending(
            eq(notificationId),
            any(),
            eq(NotificationStatus.PENDING),
            eq(NotificationStatus.PROCESSING)
        );
    }
}