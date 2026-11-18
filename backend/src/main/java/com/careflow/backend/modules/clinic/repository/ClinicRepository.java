package com.careflow.backend.modules.clinic.repository;

import com.careflow.backend.modules.clinic.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.Optional;

public interface ClinicRepository extends JpaRepository<Clinic, UUID> {

    Optional<Clinic> findByEmail(String email);

    boolean existsByEmail(String email);
}
