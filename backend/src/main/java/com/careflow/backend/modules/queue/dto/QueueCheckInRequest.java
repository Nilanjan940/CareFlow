package com.careflow.backend.modules.queue.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record QueueCheckInRequest(

    UUID patientId,

    UUID doctorId,

    UUID appointmentId,

    LocalDate queueDate,

    @Size(
        max = 1000,
        message = "Queue notes must not exceed 1000 characters."
    )
    String notes

) {

    public QueueCheckInRequest {
        if (notes != null) {
            notes = notes.trim();

            if (notes.isEmpty()) {
                notes = null;
            }
        }
    }
}