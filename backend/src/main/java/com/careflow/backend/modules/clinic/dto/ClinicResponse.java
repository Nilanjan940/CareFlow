package com.careflow.backend.modules.clinic.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClinicResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String address,
    boolean active,
    LocalDateTime createdAt
) {}
