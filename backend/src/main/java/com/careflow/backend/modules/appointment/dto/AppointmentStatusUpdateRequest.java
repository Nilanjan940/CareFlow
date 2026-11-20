package com.careflow.backend.modules.appointment.dto;

import com.careflow.backend.modules.appointment.entity.AppointmentStatus;

import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateRequest(

    @NotNull(message = "Appointment status is required.")
    AppointmentStatus status

) {
}