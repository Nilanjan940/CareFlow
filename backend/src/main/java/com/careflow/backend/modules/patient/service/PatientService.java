package com.careflow.backend.modules.patient.service;

import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.exception.ClinicNotFoundException;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;
import com.careflow.backend.modules.patient.dto.PatientRequest;
import com.careflow.backend.modules.patient.dto.PatientResponse;
import com.careflow.backend.modules.patient.entity.Patient;
import com.careflow.backend.modules.patient.exception.PatientNotFoundException;
import com.careflow.backend.modules.patient.mapper.PatientMapper;
import com.careflow.backend.modules.patient.repository.PatientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;

    public PatientService(
        PatientRepository patientRepository,
        ClinicRepository clinicRepository
    ) {
        this.patientRepository = patientRepository;
        this.clinicRepository = clinicRepository;
    }

    public PatientResponse registerPatient(
        UUID clinicId,
        PatientRequest request
    ) {

        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            ));

        Patient patient = new Patient();
        patient.setClinic(clinic);
        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        Patient savedPatient = patientRepository.save(patient);

        return PatientMapper.toResponse(savedPatient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients(UUID clinicId) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            );
        }

        return patientRepository.findAllByClinicId(clinicId)
            .stream()
            .map(PatientMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(
        UUID clinicId,
        UUID patientId
    ) {

        Patient patient = patientRepository
            .findByIdAndClinicId(patientId, clinicId)
            .orElseThrow(() -> new PatientNotFoundException(
                "Patient not found with ID: " + patientId
                    + " in clinic: " + clinicId
            ));

        return PatientMapper.toResponse(patient);
    }
}