package com.careflow.backend.modules.notification.service;

import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationClaimService {

    private final NotificationRepository notificationRepository;

    public NotificationClaimService(
        NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public boolean claim(UUID notificationId) {

        int updated = notificationRepository.claimIfPending(
            notificationId,
            LocalDateTime.now(),
            NotificationStatus.PENDING,
            NotificationStatus.PROCESSING
        );

        return updated == 1;
    }
}