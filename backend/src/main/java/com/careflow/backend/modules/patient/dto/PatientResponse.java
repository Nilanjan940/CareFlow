package com.careflow.backend.modules.patient.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    UUID clinicId,
    String clinicName,
    String name,
    String email,
    String phone,
    LocalDate dateOfBirth,
    String gender,
    String address,
    boolean active,
    LocalDateTime createdAt
) {
}