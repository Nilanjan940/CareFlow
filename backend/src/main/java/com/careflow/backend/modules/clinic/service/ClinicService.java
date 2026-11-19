package com.careflow.backend.modules.clinic.service;

import com.careflow.backend.modules.clinic.dto.ClinicRequest;
import com.careflow.backend.modules.clinic.dto.ClinicResponse;
import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.exception.ClinicNotFoundException;
import com.careflow.backend.modules.clinic.exception.DuplicateClinicException;
import com.careflow.backend.modules.clinic.mapper.ClinicMapper;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Transactional
    public ClinicResponse registerClinic(ClinicRequest request) {

        String normalizedEmail = request.email()
            .trim()
            .toLowerCase(Locale.ROOT);

        if (clinicRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateClinicException(
                "A clinic with this email already exists."
            );
        }

        ClinicRequest normalizedRequest = new ClinicRequest(
            request.name().trim(),
            normalizedEmail,
            request.phone(),
            request.address()
        );

        Clinic clinic = ClinicMapper.toEntity(normalizedRequest);

        try {
            Clinic savedClinic = clinicRepository.saveAndFlush(clinic);
            return ClinicMapper.toResponse(savedClinic);

        } catch (DataIntegrityViolationException exception) {

            if (isClinicEmailConstraintViolation(exception)) {
                throw new DuplicateClinicException(
                    "A clinic with this email already exists."
                );
            }

            throw exception;
        }
    }

    private boolean isClinicEmailConstraintViolation(
            DataIntegrityViolationException exception) {

        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof
                    org.hibernate.exception.ConstraintViolationException hibernateException) {

                String constraintName = hibernateException.getConstraintName();

                if (constraintName != null &&
                        constraintName.toLowerCase(Locale.ROOT)
                            .contains("uk_clinics_email")) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlException &&
                    sqlException.getErrorCode() == 1062) {

                String message = sqlException.getMessage();

                if (message != null &&
                        message.toLowerCase(Locale.ROOT)
                            .contains("uk_clinics_email")) {
                    return true;
                }
            }

            cause = cause.getCause();
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<ClinicResponse> getAllClinics() {

        return clinicRepository.findAll()
            .stream()
            .map(ClinicMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ClinicResponse getClinicById(UUID id) {

        Clinic clinic = clinicRepository.findById(id)
            .orElseThrow(() ->
                new ClinicNotFoundException("Clinic not found.")
            );

        return ClinicMapper.toResponse(clinic);
    }
}