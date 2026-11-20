package com.careflow.backend.modules.availability.dto;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilitySessionResponse(
    UUID id,
    UUID clinicId,
    UUID doctorId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    LocalDateTime createdAt
) {}