package com.careflow.backend.modules.patient.repository;

import com.careflow.backend.modules.patient.entity.Patient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findAllByClinicId(UUID clinicId);

    Optional<Patient> findByIdAndClinicId(UUID id, UUID clinicId);

    boolean existsByClinicIdAndPhone(UUID clinicId, String phone);
}