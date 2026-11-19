package com.careflow.backend.modules.doctor.repository;

import com.careflow.backend.modules.doctor.entity.Doctor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    List<Doctor> findAllByClinicId(UUID clinicId);

    Optional<Doctor> findByIdAndClinicId(UUID id, UUID clinicId);

    boolean existsByClinicIdAndEmail(UUID clinicId, String email);
}