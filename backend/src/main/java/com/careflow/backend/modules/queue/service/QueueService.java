package com.careflow.backend.modules.queue.service;

import com.careflow.backend.modules.appointment.entity.Appointment;
import com.careflow.backend.modules.appointment.entity.AppointmentStatus;
import com.careflow.backend.modules.appointment.repository.AppointmentRepository;
import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.exception.ClinicNotFoundException;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;
import com.careflow.backend.modules.doctor.entity.Doctor;
import com.careflow.backend.modules.doctor.exception.DoctorNotFoundException;
import com.careflow.backend.modules.doctor.repository.DoctorRepository;
import com.careflow.backend.modules.patient.entity.Patient;
import com.careflow.backend.modules.patient.repository.PatientRepository;
import com.careflow.backend.modules.queue.dto.QueueCheckInRequest;
import com.careflow.backend.modules.queue.dto.QueueEntryResponse;
import com.careflow.backend.modules.queue.dto.QueuePageResponse;
import com.careflow.backend.modules.queue.dto.QueueStatusUpdateRequest;
import com.careflow.backend.modules.queue.entity.QueueEntry;
import com.careflow.backend.modules.queue.entity.QueueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.careflow.backend.modules.queue.exception.InvalidQueueCheckInException;
import com.careflow.backend.modules.queue.exception.InvalidQueueStatusException;
import com.careflow.backend.modules.queue.exception.QueueEntryNotFoundException;
import com.careflow.backend.modules.queue.mapper.QueueEntryMapper;
import com.careflow.backend.modules.queue.repository.QueueEntryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class QueueService {

    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final QueueEntryRepository queueEntryRepository;

    public QueueService(
            ClinicRepository clinicRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            QueueEntryRepository queueEntryRepository) {

        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.queueEntryRepository = queueEntryRepository;
    }

    public QueueEntryResponse checkIn(
            UUID clinicId,
            QueueCheckInRequest request) {

        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            ));

        if (!clinic.isActive()) {
            throw new InvalidQueueCheckInException(
                "Cannot check in patients to an inactive clinic."
            );
        }

        Appointment appointment = null;
        UUID doctorId;
        UUID patientId;
        LocalDate queueDate;

        if (request.appointmentId() != null) {

            appointment = appointmentRepository
                .findByIdAndClinicId(request.appointmentId(), clinicId)
                .orElseThrow(() -> new InvalidQueueCheckInException(
                    "Appointment not found in this clinic."
                ));

            if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                    && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
                throw new InvalidQueueCheckInException(
                    "Only scheduled or confirmed appointments can be checked in."
                );
            }

            doctorId = appointment.getDoctor().getId();
            patientId = appointment.getPatient().getId();
            queueDate = appointment.getAppointmentDate();

            if (request.doctorId() != null
                    && !request.doctorId().equals(doctorId)) {
                throw new InvalidQueueCheckInException(
                    "The supplied doctor does not match the appointment."
                );
            }

            if (request.patientId() != null
                    && !request.patientId().equals(patientId)) {
                throw new InvalidQueueCheckInException(
                    "The supplied patient does not match the appointment."
                );
            }

            if (request.queueDate() != null
                    && !request.queueDate().equals(queueDate)) {
                throw new InvalidQueueCheckInException(
                    "The queue date must match the appointment date."
                );
            }

        } else {

            if (request.doctorId() == null || request.patientId() == null) {
                throw new InvalidQueueCheckInException(
                    "Doctor ID and patient ID are required for walk-in check-in."
                );
            }

            doctorId = request.doctorId();
            patientId = request.patientId();
            queueDate = request.queueDate() != null
                ? request.queueDate()
                : LocalDate.now();
        }

        // Lock the doctor row to serialize queue number allocation.
        Doctor doctor = doctorRepository
            .findByIdAndClinicIdForUpdate(doctorId, clinicId)
            .orElseThrow(() -> new InvalidQueueCheckInException(
                "Doctor not found in this clinic."
            ));

        if (!doctor.isActive()) {
            throw new InvalidQueueCheckInException(
                "Cannot check in patients with an inactive doctor."
            );
        }

        Patient patient = patientRepository
            .findByIdAndClinicId(patientId, clinicId)
            .orElseThrow(() -> new InvalidQueueCheckInException(
                "Patient not found in this clinic."
            ));

        if (!patient.isActive()) {
            throw new InvalidQueueCheckInException(
                "Cannot check in an inactive patient."
            );
        }

        if (appointment != null
                && queueEntryRepository.existsByAppointmentIdAndClinicId(
                    appointment.getId(),
                    clinicId
                )) {
            throw new InvalidQueueCheckInException(
                "This appointment has already been checked in."
            );
        }

        Integer maximumQueueNumber =
            queueEntryRepository.findMaximumQueueNumber(
                doctorId,
                queueDate
            );

        QueueEntry entry = new QueueEntry();

        entry.setClinic(clinic);
        entry.setDoctor(doctor);
        entry.setPatient(patient);
        entry.setAppointment(appointment);
        entry.setQueueDate(queueDate);
        entry.setQueueNumber(maximumQueueNumber + 1);
        entry.setStatus(QueueStatus.WAITING);
        entry.setCheckedInAt(LocalDateTime.now());
        entry.setNotes(request.notes());

        QueueEntry savedEntry = queueEntryRepository.save(entry);

        return QueueEntryMapper.toResponse(savedEntry);
    }

    public QueueEntryResponse updateStatus(
            UUID clinicId,
            UUID queueEntryId,
            QueueStatusUpdateRequest request) {

        QueueEntry entry = queueEntryRepository
            .findByIdAndClinicId(queueEntryId, clinicId)
            .orElseThrow(() -> new QueueEntryNotFoundException(
                "Queue entry not found with ID: " + queueEntryId
            ));

        QueueStatus currentStatus = entry.getStatus();
        QueueStatus requestedStatus = request.status();

        boolean validTransition = switch (currentStatus) {
            case WAITING ->
                requestedStatus == QueueStatus.IN_PROGRESS
                    || requestedStatus == QueueStatus.SKIPPED
                    || requestedStatus == QueueStatus.CANCELLED;

            case IN_PROGRESS ->
                requestedStatus == QueueStatus.COMPLETED;

            case SKIPPED ->
                requestedStatus == QueueStatus.WAITING
                    || requestedStatus == QueueStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new InvalidQueueStatusException(
                "Invalid queue status transition from "
                    + currentStatus + " to " + requestedStatus + "."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        entry.setStatus(requestedStatus);

        if (requestedStatus == QueueStatus.IN_PROGRESS) {
            entry.setStartedAt(now);
        }

        if (requestedStatus == QueueStatus.COMPLETED) {
            entry.setCompletedAt(now);
        }

        QueueEntry updatedEntry = queueEntryRepository.save(entry);

        return QueueEntryMapper.toResponse(updatedEntry);
    }

    @Transactional(readOnly = true)
    public QueuePageResponse searchQueue(
        UUID clinicId,
        UUID doctorId,
        LocalDate queueDate,
        QueueStatus status,
        Pageable pageable
    ) {

        if (!clinicRepository.existsById(clinicId)) {
            throw new ClinicNotFoundException(
                "Clinic not found with ID: " + clinicId
            );
        }

        doctorRepository.findByIdAndClinicId(doctorId, clinicId)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor not found with ID: " + doctorId
            ));

        Page<QueueEntryResponse> result = queueEntryRepository.searchQueue(
            clinicId,
            doctorId,
            queueDate,
            status,
            pageable
        ).map(QueueEntryMapper::toResponse);

        return new QueuePageResponse(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );
    }

    @Transactional(readOnly = true)
    public QueueEntryResponse getQueueEntryById(
        UUID clinicId,
        UUID queueEntryId
    ) {

        QueueEntry entry = queueEntryRepository
            .findByIdAndClinicId(queueEntryId, clinicId)
            .orElseThrow(() -> new QueueEntryNotFoundException(
                "Queue entry not found with ID: " + queueEntryId
            ));

        return QueueEntryMapper.toResponse(entry);
    }
}