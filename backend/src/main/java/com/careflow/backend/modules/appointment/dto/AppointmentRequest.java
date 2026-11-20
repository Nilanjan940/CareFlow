package com.careflow.backend.modules.appointment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentRequest(

    @NotNull(message = "Doctor ID is required.")
    UUID doctorId,

    @NotNull(message = "Patient ID is required.")
    UUID patientId,

    @NotNull(message = "Appointment date is required.")
    @FutureOrPresent(message = "Appointment date cannot be in the past.")
    LocalDate appointmentDate,

    @NotNull(message = "Appointment start time is required.")
    LocalTime startTime,

    @NotNull(message = "Appointment end time is required.")
    LocalTime endTime,

    @Size(max = 500, message = "Reason cannot exceed 500 characters.")
    String reason,

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters.")
    String notes

) {
    public AppointmentRequest {
        if (reason != null) {
            reason = reason.trim();
            if (reason.isEmpty()) {
                reason = null;
            }
        }

        if (notes != null) {
            notes = notes.trim();
            if (notes.isEmpty()) {
                notes = null;
            }
        }
    }
}