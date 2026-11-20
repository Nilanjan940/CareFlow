package com.careflow.backend.modules.notification;

import com.careflow.backend.modules.appointment.entity.Appointment;
import com.careflow.backend.modules.appointment.entity.AppointmentStatus;
import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.doctor.entity.Doctor;
import com.careflow.backend.modules.patient.entity.Patient;
import com.careflow.backend.modules.notification.config.NotificationProperties;
import com.careflow.backend.modules.notification.entity.Notification;
import com.careflow.backend.modules.notification.entity.NotificationChannel;
import com.careflow.backend.modules.notification.entity.NotificationStatus;
import com.careflow.backend.modules.notification.entity.NotificationType;
import com.careflow.backend.modules.notification.repository.NotificationRepository;
import com.careflow.backend.modules.notification.service.NotificationSchedulingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulingServiceTests {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationSchedulingService service;
    private Clinic clinic;
    private Patient patient;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        NotificationProperties properties = new NotificationProperties();
        properties.setReminderHoursBefore(24);

        service = new NotificationSchedulingService(
            notificationRepository,
            properties
        );

        clinic = new Clinic();
        patient = new Patient();
        patient.setClinic(clinic);
        patient.setName("Test Patient");
        patient.setEmail("patient@careflow.test");
        patient.setPhone("9876543210");

        Doctor doctor = new Doctor();
        doctor.setClinic(clinic);

        appointment = new Appointment();
        appointment.setClinic(clinic);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        // Schedule the appointment two days in the future.
        appointment.setAppointmentDate(LocalDate.now().plusDays(2));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));


    }

    @Test
    void shouldScheduleEmailAndSmsRemindersForFutureAppointment() {
        when(notificationRepository.findAllByAppointmentId(any()))
            .thenReturn(List.of());

        service.scheduleAppointmentReminders(appointment);

        ArgumentCaptor<Notification> captor =
            ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();

        assertEquals(2, notifications.size());

        assertTrue(notifications.stream().allMatch(notification ->
            notification.getStatus() == NotificationStatus.PENDING
        ));

        assertTrue(notifications.stream().allMatch(notification ->
            notification.getNotificationType()
                == NotificationType.APPOINTMENT_REMINDER
        ));

        assertTrue(notifications.stream().anyMatch(notification ->
            notification.getChannel() == NotificationChannel.EMAIL
        ));

        assertTrue(notifications.stream().anyMatch(notification ->
            notification.getChannel() == NotificationChannel.SMS
        ));

        LocalDateTime expected = LocalDateTime.of(
            appointment.getAppointmentDate(),
            appointment.getStartTime()
        ).minusHours(24);

        assertTrue(notifications.stream().allMatch(notification ->
            notification.getScheduledAt().equals(expected)
        ));
    }

    @Test
    void shouldSkipReminderWhenAppointmentIsAlreadyInThePast() {
        appointment.setAppointmentDate(LocalDate.now().minusDays(1));

        service.scheduleAppointmentReminders(appointment);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldCancelPendingRemindersWhenAppointmentIsCancelled() {
        appointment.setStatus(AppointmentStatus.CANCELLED);

        Notification email = createPendingNotification(
            NotificationChannel.EMAIL
        );
        Notification sms = createPendingNotification(
            NotificationChannel.SMS
        );

        when(notificationRepository.findAllByAppointmentIdAndStatus(
            any(),
            eq(NotificationStatus.PENDING)
        )).thenReturn(List.of(email, sms));

        service.cancelAppointmentReminders(appointment);

        assertEquals(NotificationStatus.CANCELLED, email.getStatus());
        assertEquals(NotificationStatus.CANCELLED, sms.getStatus());

        verify(notificationRepository).saveAll(List.of(email, sms));
    }

    @Test
    void shouldUpdateExistingPendingReminderWhenAppointmentIsRescheduled() {
        Notification existingEmail = createPendingNotification(
            NotificationChannel.EMAIL
        );

        when(notificationRepository.findAllByAppointmentId(any()))
            .thenReturn(List.of(existingEmail));

        appointment.setAppointmentDate(LocalDate.now().plusDays(3));

        service.rescheduleAppointmentReminders(appointment);

        verify(notificationRepository).save(existingEmail);

        assertEquals(
            LocalDateTime.of(
                appointment.getAppointmentDate(),
                appointment.getStartTime()
            ).minusHours(24),
            existingEmail.getScheduledAt()
        );

        assertEquals(patient, existingEmail.getPatient());
        assertEquals(clinic, existingEmail.getClinic());
        assertEquals(appointment, existingEmail.getAppointment());
    }

    @Test
    void shouldReactivateCancelledRemindersWhenAppointmentIsRescheduled() {
        Notification email = createPendingNotification(
            NotificationChannel.EMAIL
        );
        Notification sms = createPendingNotification(
            NotificationChannel.SMS
        );

        email.setStatus(NotificationStatus.CANCELLED);
        sms.setStatus(NotificationStatus.CANCELLED);

        when(notificationRepository.findAllByAppointmentId(any()))
            .thenReturn(
                List.of(email, sms),
                List.of(email, sms)
            );

        appointment.setAppointmentDate(LocalDate.now().plusDays(3));

        service.rescheduleAppointmentReminders(appointment);

        assertEquals(NotificationStatus.PENDING, email.getStatus());
        assertEquals(NotificationStatus.PENDING, sms.getStatus());

        verify(notificationRepository).save(email);
        verify(notificationRepository).save(sms);
    }

    private Notification createPendingNotification(
        NotificationChannel channel
    ) {
        Notification notification = new Notification();
        notification.setClinic(clinic);
        notification.setAppointment(appointment);
        notification.setPatient(patient);
        notification.setNotificationType(
            NotificationType.APPOINTMENT_REMINDER
        );
        notification.setChannel(channel);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledAt(LocalDateTime.now().plusHours(1));
        return notification;
    }
}