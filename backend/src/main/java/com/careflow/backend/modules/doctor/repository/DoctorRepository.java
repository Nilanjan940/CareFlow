package com.careflow.backend.modules.doctor.repository;

import com.careflow.backend.modules.doctor.entity.Doctor;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findAllByClinicId(UUID clinicId);

    Optional<Doctor> findByIdAndClinicId(UUID id, UUID clinicId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT d
        FROM Doctor d
        WHERE d.id = :doctorId
          AND d.clinic.id = :clinicId
        """)
    Optional<Doctor> findByIdAndClinicIdForUpdate(
        @Param("doctorId") UUID doctorId,
        @Param("clinicId") UUID clinicId
    );

    boolean existsByClinicIdAndEmail(UUID clinicId, String email);
}