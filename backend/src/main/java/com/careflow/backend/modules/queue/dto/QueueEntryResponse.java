package com.careflow.backend.modules.queue.dto;

import com.careflow.backend.modules.queue.entity.QueueStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record QueueEntryResponse(

    UUID id,

    UUID clinicId,

    UUID doctorId,

    UUID patientId,

    UUID appointmentId,

    LocalDate queueDate,

    Integer queueNumber,

    QueueStatus status,

    LocalDateTime checkedInAt,

    LocalDateTime startedAt,

    LocalDateTime completedAt,

    String notes,

    LocalDateTime createdAt

) {
}