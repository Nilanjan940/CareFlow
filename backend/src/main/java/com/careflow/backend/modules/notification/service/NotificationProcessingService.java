package com.careflow.backend.modules.notification.service;

import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationProcessingService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService deliveryService;

    public NotificationProcessingService(
        NotificationRepository notificationRepository,
        NotificationDeliveryService deliveryService
    ) {
        this.notificationRepository = notificationRepository;
        this.deliveryService = deliveryService;
    }

    @Transactional
    public void processClaimedNotification(UUID notificationId) {

        Notification notification = notificationRepository
            .findById(notificationId)
            .orElse(null);

        if (notification == null
            || notification.getStatus() != NotificationStatus.PROCESSING) {
            return;
        }

        try {
            deliveryService.deliver(notification);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notification.setErrorMessage(null);

        } catch (RuntimeException exception) {

            notification.setStatus(NotificationStatus.FAILED);

            String errorMessage = exception.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Notification delivery failed.";
            }

            notification.setErrorMessage(
                errorMessage.length() > 1000
                    ? errorMessage.substring(0, 1000)
                    : errorMessage
            );
        }

        notificationRepository.save(notification);
    }
}