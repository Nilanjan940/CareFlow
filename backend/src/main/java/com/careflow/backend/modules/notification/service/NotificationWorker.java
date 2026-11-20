package com.careflow.backend.modules.notification.service;

import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.repository.NotificationRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationWorker {

    private final NotificationRepository notificationRepository;
    private final NotificationClaimService claimService;
    private final NotificationProcessingService processingService;

    public NotificationWorker(
        NotificationRepository notificationRepository,
        NotificationClaimService claimService,
        NotificationProcessingService processingService
    ) {
        this.notificationRepository = notificationRepository;
        this.claimService = claimService;
        this.processingService = processingService;
    }

    @Scheduled(
        fixedDelayString = "${careflow.notifications.scheduler-interval-ms:60000}"
    )
    public void processDueNotifications() {

        List<Notification> dueNotifications =
            notificationRepository.findAllByStatusAndScheduledAtLessThanEqual(
                NotificationStatus.PENDING,
                LocalDateTime.now()
            );

        for (Notification notification : dueNotifications) {

            UUID notificationId = notification.getId();

            if (claimService.claim(notificationId)) {
                processingService.processClaimedNotification(
                    notificationId
                );
            }
        }
    }
}