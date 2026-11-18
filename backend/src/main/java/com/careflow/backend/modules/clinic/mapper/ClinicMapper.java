package com.careflow.backend.modules.clinic.mapper;

import com.careflow.backend.modules.clinic.dto.ClinicRequest;
import com.careflow.backend.modules.clinic.dto.ClinicResponse;
import com.careflow.backend.modules.clinic.entity.Clinic;

public class ClinicMapper {

    private ClinicMapper() {
    }

    public static Clinic toEntity(ClinicRequest request) {
        Clinic clinic = new Clinic();

        clinic.setName(request.name());
        clinic.setEmail(request.email());
        clinic.setPhone(request.phone());
        clinic.setAddress(request.address());

        return clinic;
    }

    public static ClinicResponse toResponse(Clinic clinic) {
        return new ClinicResponse(
            clinic.getId(),
            clinic.getName(),
            clinic.getEmail(),
            clinic.getPhone(),
            clinic.getAddress(),
            clinic.isActive(),
            clinic.getCreatedAt()
        );
    }
}
