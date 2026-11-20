package com.careflow.backend.modules.availability.repository;

import com.careflow.backend.modules.availability.entity.AvailabilitySession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvailabilitySessionRepository
    extends JpaRepository<AvailabilitySession, UUID> {

    List<AvailabilitySession> findAllByClinicIdAndDoctorIdOrderByDayOfWeekAscStartTimeAsc(
        UUID clinicId,
        UUID doctorId
    );

    List<AvailabilitySession> findAllByClinicIdAndDoctorIdAndDayOfWeekOrderByStartTimeAsc(
        UUID clinicId,
        UUID doctorId,
        DayOfWeek dayOfWeek
    );

    boolean existsByClinicIdAndDoctorId(UUID clinicId, UUID doctorId);

    Optional<AvailabilitySession> findByIdAndClinicId(
        UUID id,
        UUID clinicId
    );
}