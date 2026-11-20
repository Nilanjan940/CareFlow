package com.careflow.backend.modules.appointment.service;

import com.careflow.backend.modules.appointment.dto.AppointmentRequest;
import com.careflow.backend.modules.appointment.dto.AppointmentResponse;
import com.careflow.backend.modules.appointment.dto.AppointmentStatusUpdateRequest;
import com.careflow.backend.modules.appointment.entity.Appointment;
import com.careflow.backend.modules.appointment.entity.AppointmentStatus;
import com.careflow.backend.modules.appointment.exception.AppointmentConflictException;
import com.careflow.backend.modules.appointment.exception.AppointmentNotFoundException;
import com.careflow.backend.modules.appointment.exception.AppointmentUpdateNotAllowedException;
import com.careflow.backend.modules.appointment.exception.InvalidAppointmentTimeException;
import com.careflow.backend.modules.appointment.exception.InvalidAppointmentStatusException;
import com.careflow.backend.modules.appointment.mapper.AppointmentMapper;
import com.careflow.backend.modules.appointment.repository.AppointmentRepository;
import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.exception.ClinicNotFoundException;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;
import com.careflow.backend.modules.doctor.entity.Doctor;
import com.careflow.backend.modules.doctor.exception.DoctorNotFoundException;
import com.careflow.backend.modules.doctor.repository.DoctorRepository;
import com.careflow.backend.modules.patient.entity.Patient;
import com.careflow.backend.modules.patient.exception.PatientNotFoundException;
import com.careflow.backend.modules.patient.repository.PatientRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentHistoryService appointmentHistoryService;

    public AppointmentService(
        AppointmentRepository appointmentRepository,
        ClinicRepository clinicRepository,
        DoctorRepository doctorRepository,
        PatientRepository patientRepository,
        AppointmentHistoryService appointmentHistoryService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentHistoryService = appointmentHistoryService;
    }

    public AppointmentResponse createAppointment(
        UUID clinicId,
        AppointmentRequest request
    ) {

        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            ));

        Doctor doctor = doctorRepository.findByIdAndClinicId(
            request.doctorId(),
            clinicId
        ).orElseThrow(() -> new DoctorNotFoundException(
            "Doctor not found in this clinic with ID: " + request.doctorId()
        ));

        Patient patient = patientRepository.findByIdAndClinicId(
            request.patientId(),
            clinicId
        ).orElseThrow(() -> new PatientNotFoundException(
            "Patient not found in this clinic with ID: " + request.patientId()
        ));

        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidAppointmentTimeException(
                "Appointment end time must be after start time."
            );
        }

        boolean overlapping = appointmentRepository.existsOverlappingAppointment(
            doctor.getId(),
            request.appointmentDate(),
            request.startTime(),
            request.endTime(),
            AppointmentStatus.CANCELLED
        );

        if (overlapping) {
            throw new AppointmentConflictException(
                "The doctor already has an appointment during this time."
            );
        }

        Appointment appointment = new Appointment();

        appointment.setClinic(clinic);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setReason(request.reason());
        appointment.setNotes(request.notes());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        appointmentHistoryService.recordCreated(savedAppointment);

        return AppointmentMapper.toResponse(savedAppointment);
    }

    public AppointmentResponse updateAppointment(
        UUID clinicId,
        UUID appointmentId,
        AppointmentRequest request
    ) {

        Appointment appointment = appointmentRepository
            .findByIdAndClinicId(appointmentId, clinicId)
            .orElseThrow(() -> new AppointmentNotFoundException(
                "Appointment not found with ID: " + appointmentId
            ));

        Map<String, Object> oldValues =
            appointmentHistoryService.createSnapshot(appointment);

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
            appointment.getStatus() == AppointmentStatus.CANCELLED) {

            throw new AppointmentUpdateNotAllowedException(
                "Completed or cancelled appointments cannot be updated."
            );
        }

        Doctor doctor = doctorRepository.findByIdAndClinicId(
            request.doctorId(),
            clinicId
        ).orElseThrow(() -> new DoctorNotFoundException(
            "Doctor not found in this clinic with ID: " + request.doctorId()
        ));

        Patient patient = patientRepository.findByIdAndClinicId(
            request.patientId(),
            clinicId
        ).orElseThrow(() -> new PatientNotFoundException(
            "Patient not found in this clinic with ID: " + request.patientId()
        ));

        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidAppointmentTimeException(
                "Appointment end time must be after start time."
            );
        }

        boolean overlapping =
            appointmentRepository.existsOverlappingAppointmentExcludingId(
                doctor.getId(),
                request.appointmentDate(),
                request.startTime(),
                request.endTime(),
                AppointmentStatus.CANCELLED,
                appointmentId
            );

        if (overlapping) {
            throw new AppointmentConflictException(
                "The doctor already has an appointment during this time."
            );
        }

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setReason(request.reason());
        appointment.setNotes(request.notes());

        Appointment updatedAppointment =
            appointmentRepository.save(appointment);

        appointmentHistoryService.recordUpdated(
            updatedAppointment,
            oldValues
        );

        return AppointmentMapper.toResponse(updatedAppointment);
    }
    public AppointmentResponse updateAppointmentStatus(
        UUID clinicId,
        UUID appointmentId,
        AppointmentStatusUpdateRequest request
    ) {

        Appointment appointment = appointmentRepository
            .findByIdAndClinicId(appointmentId, clinicId)
            .orElseThrow(() -> new AppointmentNotFoundException(
                "Appointment not found with ID: " + appointmentId
            ));

        AppointmentStatus currentStatus = appointment.getStatus();
        AppointmentStatus requestedStatus = request.status();

        boolean validTransition = switch (currentStatus) {
            case SCHEDULED ->
                requestedStatus == AppointmentStatus.CONFIRMED ||
                requestedStatus == AppointmentStatus.CANCELLED;

            case CONFIRMED ->
                requestedStatus == AppointmentStatus.COMPLETED ||
                requestedStatus == AppointmentStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new InvalidAppointmentStatusException(
                "Cannot change appointment status from " +
                currentStatus + " to " + requestedStatus + "."
            );
        }

        appointment.setStatus(requestedStatus);

        Appointment updatedAppointment =
            appointmentRepository.save(appointment);

        appointmentHistoryService.recordStatusChanged(
            updatedAppointment,
            currentStatus.name(),
            requestedStatus.name()
        );

        return AppointmentMapper.toResponse(updatedAppointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> searchAppointments(
        UUID clinicId,
        LocalDate appointmentDate,
        AppointmentStatus status,
        UUID doctorId,
        UUID patientId,
        Pageable pageable
    ) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            );
        }

        return appointmentRepository.searchAppointments(
            clinicId,
            appointmentDate,
            status,
            doctorId,
            patientId,
            pageable
        ).map(AppointmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments(UUID clinicId) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            );
        }

        return appointmentRepository.findAllByClinicId(clinicId)
            .stream()
            .map(AppointmentMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(
        UUID clinicId,
        UUID appointmentId
    ) {

        Appointment appointment = appointmentRepository
            .findByIdAndClinicId(appointmentId, clinicId)
            .orElseThrow(() -> new AppointmentNotFoundException(
                "Appointment not found with ID: " + appointmentId
            ));

        return AppointmentMapper.toResponse(appointment);
    }
}