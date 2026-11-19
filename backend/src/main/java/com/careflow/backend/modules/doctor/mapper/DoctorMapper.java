package com.careflow.backend.modules.doctor.mapper;

import com.careflow.backend.modules.doctor.dto.DoctorResponse;
import com.careflow.backend.modules.doctor.entity.Doctor;

public class DoctorMapper {

    private DoctorMapper() {
    }

    public static DoctorResponse toResponse(Doctor doctor) {

        return new DoctorResponse(
            doctor.getId(),
            doctor.getClinic().getId(),
            doctor.getClinic().getName(),
            doctor.getName(),
            doctor.getEmail(),
            doctor.getPhone(),
            doctor.getSpecialization(),
            doctor.isActive(),
            doctor.getCreatedAt()
        );
    }
}