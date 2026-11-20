package com.careflow.backend.modules.appointment.dto;

import com.careflow.backend.modules.appointment.entity.AppointmentHistoryAction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AppointmentHistoryResponse(
    UUID id,
    AppointmentHistoryAction action,
    String description,
    Map<String, Object> oldValues,
    Map<String, Object> newValues,
    LocalDateTime createdAt
) {
}