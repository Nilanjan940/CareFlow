package com.careflow.backend.modules.doctor.service;

import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.exception.ClinicNotFoundException;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;
import com.careflow.backend.modules.doctor.dto.DoctorRequest;
import com.careflow.backend.modules.doctor.dto.DoctorResponse;
import com.careflow.backend.modules.doctor.entity.Doctor;
import com.careflow.backend.modules.doctor.exception.DoctorNotFoundException;
import com.careflow.backend.modules.doctor.exception.DuplicateDoctorException;
import com.careflow.backend.modules.doctor.mapper.DoctorMapper;
import com.careflow.backend.modules.doctor.repository.DoctorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ClinicRepository clinicRepository;

    public DoctorService(
        DoctorRepository doctorRepository,
        ClinicRepository clinicRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.clinicRepository = clinicRepository;
    }

    public DoctorResponse registerDoctor(UUID clinicId, DoctorRequest request) {

        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            ));

        if (doctorRepository.existsByClinicIdAndEmail(
            clinicId,
            request.email()
        )) {
            throw new DuplicateDoctorException(
                "A doctor with this email already exists in this clinic."
            );
        }

        Doctor doctor = new Doctor();
        doctor.setClinic(clinic);
        doctor.setName(request.name());
        doctor.setEmail(request.email());
        doctor.setPhone(request.phone());
        doctor.setSpecialization(request.specialization());

        Doctor savedDoctor = doctorRepository.save(doctor);

        return DoctorMapper.toResponse(savedDoctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors(UUID clinicId) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            );
        }

        return doctorRepository.findAllByClinicId(clinicId)
            .stream()
            .map(DoctorMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID clinicId, UUID doctorId) {

        Doctor doctor = doctorRepository
            .findByIdAndClinicId(doctorId, clinicId)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor not found with ID: " + doctorId
                    + " in clinic: " + clinicId
            ));

        return DoctorMapper.toResponse(doctor);
    }
}