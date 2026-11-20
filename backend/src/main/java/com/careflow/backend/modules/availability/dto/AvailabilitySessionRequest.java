package com.careflow.backend.modules.availability.dto;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilitySessionRequest(

    @NotNull(message = "Day of week is required.")
    DayOfWeek dayOfWeek,

    @NotNull(message = "Session start time is required.")
    LocalTime startTime,

    @NotNull(message = "Session end time is required.")
    LocalTime endTime

) {}