package com.careflow.backend.modules.appointment.dto;

import com.careflow.backend.modules.appointment.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponse(

    UUID id,

    UUID clinicId,
    String clinicName,

    UUID doctorId,
    String doctorName,

    UUID patientId,
    String patientName,

    LocalDate appointmentDate,
    LocalTime startTime,
    LocalTime endTime,

    String reason,
    AppointmentStatus status,
    String notes,

    LocalDateTime createdAt

) {
}