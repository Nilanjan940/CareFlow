package com.careflow.backend.modules.availability.mapper;

import com.careflow.backend.modules.availability.dto.AvailabilitySessionResponse;
import com.careflow.backend.modules.availability.entity.AvailabilitySession;

public final class AvailabilitySessionMapper {

    private AvailabilitySessionMapper() {}

    public static AvailabilitySessionResponse toResponse(
        AvailabilitySession session
    ) {
        return new AvailabilitySessionResponse(
            session.getId(),
            session.getClinic().getId(),
            session.getDoctor().getId(),
            session.getDayOfWeek(),
            session.getStartTime(),
            session.getEndTime(),
            session.getCreatedAt()
        );
    }
}