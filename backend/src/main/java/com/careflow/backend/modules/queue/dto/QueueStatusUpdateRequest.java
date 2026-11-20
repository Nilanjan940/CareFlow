package com.careflow.backend.modules.queue.dto;

import com.careflow.backend.modules.queue.entity.QueueStatus;

import jakarta.validation.constraints.NotNull;

public record QueueStatusUpdateRequest(

    @NotNull(message = "Queue status is required.")
    QueueStatus status

) {}