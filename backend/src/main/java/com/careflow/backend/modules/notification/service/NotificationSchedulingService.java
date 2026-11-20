package com.careflow.backend.modules.notification.service;

import com.careflow.backend.modules.appointment.entity.Appointment;
import com.careflow.backend.modules.appointment.entity.AppointmentStatus;
import com.careflow.backend.modules.notification.config.NotificationProperties;
import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationChannel;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.entity.NotificationType;
import com.careflow.backend.modules.notification.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificationSchedulingService {

    private final NotificationRepository notificationRepository;
    private final NotificationProperties notificationProperties;

    public NotificationSchedulingService(
        NotificationRepository notificationRepository,
        NotificationProperties notificationProperties
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationProperties = notificationProperties;
    }

    public void scheduleAppointmentReminders(Appointment appointment) {

        if (!isEligible(appointment)) {
            return;
        }

        LocalDateTime scheduledAt = calculateScheduledAt(appointment);

        if (scheduledAt == null) {
            return;
        }

        scheduleChannelIfAvailable(
            appointment,
            NotificationChannel.EMAIL,
            appointment.getPatient().getEmail(),
            scheduledAt
        );

        scheduleChannelIfAvailable(
            appointment,
            NotificationChannel.SMS,
            appointment.getPatient().getPhone(),
            scheduledAt
        );
    }

    public void rescheduleAppointmentReminders(Appointment appointment) {

        if (!isEligible(appointment)) {
            cancelAppointmentReminders(appointment);
            return;
        }

        LocalDateTime scheduledAt = calculateScheduledAt(appointment);

        if (scheduledAt == null) {
            cancelAppointmentReminders(appointment);
            return;
        }

        scheduleAppointmentReminders(appointment);
    }

    public void cancelAppointmentReminders(Appointment appointment) {

        List<Notification> pendingNotifications =
            notificationRepository.findAllByAppointmentIdAndStatus(
                appointment.getId(),
                NotificationStatus.PENDING
            );

        pendingNotifications.forEach(notification ->
            notification.setStatus(NotificationStatus.CANCELLED)
        );

        notificationRepository.saveAll(pendingNotifications);
    }

    private LocalDateTime calculateScheduledAt(Appointment appointment) {

        LocalDateTime appointmentDateTime = LocalDateTime.of(
            appointment.getAppointmentDate(),
            appointment.getStartTime()
        );

        LocalDateTime now = LocalDateTime.now();

        if (!appointmentDateTime.isAfter(now)) {
            return null;
        }

        LocalDateTime scheduledAt = appointmentDateTime.minusHours(
            notificationProperties.getReminderHoursBefore()
        );

        return scheduledAt.isBefore(now) ? now : scheduledAt;
    }

    private void scheduleChannelIfAvailable(
        Appointment appointment,
        NotificationChannel channel,
        String contact,
        LocalDateTime scheduledAt
    ) {

        if (contact == null || contact.isBlank()) {
            cancelPendingReminderForChannel(appointment, channel);
            return;
        }

        createOrUpdateReminder(appointment, channel, scheduledAt);
    }

    private void cancelPendingReminderForChannel(
        Appointment appointment,
        NotificationChannel channel
    ) {

        List<Notification> notifications =
            notificationRepository.findAllByAppointmentId(
                appointment.getId()
            );

        notifications.stream()
            .filter(notification ->
                notification.getNotificationType()
                    == NotificationType.APPOINTMENT_REMINDER
                    && notification.getChannel() == channel
                    && notification.getStatus() == NotificationStatus.PENDING
            )
            .forEach(notification ->
                notification.setStatus(NotificationStatus.CANCELLED)
            );
    }

    private void createOrUpdateReminder(
        Appointment appointment,
        NotificationChannel channel,
        LocalDateTime scheduledAt
    ) {

        List<Notification> existingNotifications =
            notificationRepository.findAllByAppointmentId(
                appointment.getId()
            );

        Notification existing = existingNotifications.stream()
            .filter(notification ->
                notification.getNotificationType()
                    == NotificationType.APPOINTMENT_REMINDER
                    && notification.getChannel() == channel
            )
            .findFirst()
            .orElse(null);

        if (existing != null) {

            if (existing.getStatus() == NotificationStatus.PENDING
                || existing.getStatus() == NotificationStatus.CANCELLED) {

                existing.setStatus(NotificationStatus.PENDING);
                existing.setScheduledAt(scheduledAt);
                existing.setPatient(appointment.getPatient());
                existing.setClinic(appointment.getClinic());
                existing.setAppointment(appointment);

                notificationRepository.save(existing);
            }

            return;
        }

        Notification notification = new Notification();

        notification.setClinic(appointment.getClinic());
        notification.setAppointment(appointment);
        notification.setPatient(appointment.getPatient());
        notification.setNotificationType(
            NotificationType.APPOINTMENT_REMINDER
        );
        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledAt(scheduledAt);

        notificationRepository.save(notification);
    }
    private boolean isEligible(Appointment appointment) {

        return appointment.getStatus() == AppointmentStatus.SCHEDULED
            || appointment.getStatus() == AppointmentStatus.CONFIRMED;
    }
}