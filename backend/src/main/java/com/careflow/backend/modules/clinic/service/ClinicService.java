package com.careflow.backend.modules.clinic.service;

import com.careflow.backend.modules.clinic.dto.ClinicRequest;
import com.careflow.backend.modules.clinic.dto.ClinicResponse;
import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.exception.ClinicNotFoundException;
import com.careflow.backend.modules.clinic.exception.DuplicateClinicException;
import com.careflow.backend.modules.clinic.mapper.ClinicMapper;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public ClinicService(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Transactional
    public ClinicResponse registerClinic(ClinicRequest request) {

        if (clinicRepository.existsByEmail(request.email())) {
            throw new DuplicateClinicException(
                "A clinic with this email already exists."
            );
        }

        Clinic clinic = ClinicMapper.toEntity(request);
        Clinic savedClinic = clinicRepository.save(clinic);

        return ClinicMapper.toResponse(savedClinic);
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
