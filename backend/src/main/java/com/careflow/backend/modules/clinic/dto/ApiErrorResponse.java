package com.careflow.backend.modules.clinic.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path
) {}
