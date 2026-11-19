package com.careflow.backend.modules.doctor.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorResponse(
    UUID id,
    UUID clinicId,
    String clinicName,
    String name,
    String email,
    String phone,
    String specialization,
    boolean active,
    LocalDateTime createdAt
) {
}