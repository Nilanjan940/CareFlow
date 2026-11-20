package com.careflow.backend.modules.notification.repository;

import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationChannel;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.entity.NotificationType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
    extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByStatusAndScheduledAtLessThanEqual(
        NotificationStatus status,
        LocalDateTime scheduledAt
    );

    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.status = :processing
        WHERE n.id = :id
          AND n.status = :pending
          AND n.scheduledAt <= :now
        """)
    int claimIfPending(
        @Param("id") UUID id,
        @Param("now") LocalDateTime now,
        @Param("pending") NotificationStatus pending,
        @Param("processing") NotificationStatus processing
    );

    List<Notification> findAllByClinicId(UUID clinicId);

    Optional<Notification> findByIdAndClinicId(
        UUID id,
        UUID clinicId
    );

    boolean existsByAppointmentIdAndNotificationTypeAndChannel(
        UUID appointmentId,
        NotificationType notificationType,
        NotificationChannel channel
    );

    List<Notification> findAllByAppointmentId(UUID appointmentId);

    List<Notification> findAllByAppointmentIdAndStatus(
        UUID appointmentId,
        NotificationStatus status
    );
}