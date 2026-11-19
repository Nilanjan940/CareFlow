package com.careflow.backend.modules.patient.mapper;

import com.careflow.backend.modules.patient.dto.PatientResponse;
import com.careflow.backend.modules.patient.entity.Patient;

public class PatientMapper {

    private PatientMapper() {
    }

    public static PatientResponse toResponse(Patient patient) {

        return new PatientResponse(
            patient.getId(),
            patient.getClinic().getId(),
            patient.getClinic().getName(),
            patient.getName(),
            patient.getEmail(),
            patient.getPhone(),
            patient.getDateOfBirth(),
            patient.getGender(),
            patient.getAddress(),
            patient.isActive(),
            patient.getCreatedAt()
        );
    }
}