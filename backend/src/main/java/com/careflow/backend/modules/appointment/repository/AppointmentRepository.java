package com.careflow.backend.modules.appointment.repository;

import com.careflow.backend.modules.appointment.entity.Appointment;
import com.careflow.backend.modules.appointment.entity.AppointmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID> {

    List<Appointment> findAllByClinicId(UUID clinicId);

    Optional<Appointment> findByIdAndClinicId(
        UUID id,
        UUID clinicId
    );

    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate = :appointmentDate
          AND a.status <> :cancelledStatus
          AND a.startTime < :endTime
          AND a.endTime > :startTime
        """)
    boolean existsOverlappingAppointment(
        @Param("doctorId") UUID doctorId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );
    @Query("""
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.appointmentDate = :appointmentDate
          AND a.id <> :excludedAppointmentId
          AND a.status <> :cancelledStatus
          AND a.startTime < :endTime
          AND a.endTime > :startTime
        """)
    boolean existsOverlappingAppointmentExcludingId(
        @Param("doctorId") UUID doctorId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("cancelledStatus") AppointmentStatus cancelledStatus,
        @Param("excludedAppointmentId") UUID excludedAppointmentId
    );
}