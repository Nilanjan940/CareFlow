package com.careflow.backend.modules.appointment.dto;

import jakarta.validation.constraints.Size;

public record AppointmentCancellationRequest(

    @Size(
        max = 500,
        message = "Cancellation reason must not exceed 500 characters."
    )
    String reason

) {

    public AppointmentCancellationRequest {
        if (reason != null) {
            reason = reason.trim();

            if (reason.isEmpty()) {
                reason = null;
            }
        }
    }
}