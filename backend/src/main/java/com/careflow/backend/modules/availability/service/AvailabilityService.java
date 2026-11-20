package com.careflow.backend.modules.availability.service;

import com.careflow.backend.modules.availability.dto.AvailabilitySessionRequest;
import com.careflow.backend.modules.availability.dto.AvailabilitySessionResponse;
import com.careflow.backend.modules.availability.entity.AvailabilitySession;
import com.careflow.backend.modules.availability.exception.AvailabilityConflictException;
import com.careflow.backend.modules.appointment.exception.AppointmentOutsideAvailabilityException;
import java.time.LocalDate;
import java.time.LocalTime;
import com.careflow.backend.modules.availability.exception.InvalidAvailabilitySessionException;
import com.careflow.backend.modules.availability.mapper.AvailabilitySessionMapper;
import com.careflow.backend.modules.availability.repository.AvailabilitySessionRepository;
import com.careflow.backend.modules.clinic.entity.Clinic;
import com.careflow.backend.modules.clinic.repository.ClinicRepository;
import com.careflow.backend.modules.doctor.entity.Doctor;
import com.careflow.backend.modules.doctor.repository.DoctorRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AvailabilityService {

    private final AvailabilitySessionRepository availabilitySessionRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;

    public AvailabilityService(
        AvailabilitySessionRepository availabilitySessionRepository,
        ClinicRepository clinicRepository,
        DoctorRepository doctorRepository
    ) {
        this.availabilitySessionRepository = availabilitySessionRepository;
        this.clinicRepository = clinicRepository;
        this.doctorRepository = doctorRepository;
    }

    public AvailabilitySessionResponse createSession(
        UUID clinicId,
        UUID doctorId,
        AvailabilitySessionRequest request
    ) {
        Clinic clinic = clinicRepository.findById(clinicId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Clinic not found."
            ));

        Doctor doctor = doctorRepository.findByIdAndClinicIdForUpdate(
            doctorId,
            clinicId
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Doctor not found in this clinic."
        ));

        validateTimeRange(request);

        List<AvailabilitySession> existingSessions =
            availabilitySessionRepository
                .findAllByClinicIdAndDoctorIdAndDayOfWeekOrderByStartTimeAsc(
                    clinicId,
                    doctorId,
                    request.dayOfWeek()
                );

        boolean overlaps = existingSessions.stream().anyMatch(existing ->
            request.startTime().isBefore(existing.getEndTime())
                && request.endTime().isAfter(existing.getStartTime())
        );

        if (overlaps) {
            throw new AvailabilityConflictException(
                "This availability session overlaps an existing session."
            );
        }

        AvailabilitySession session = new AvailabilitySession();
        session.setClinic(clinic);
        session.setDoctor(doctor);
        session.setDayOfWeek(request.dayOfWeek());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());

        AvailabilitySession savedSession =
            availabilitySessionRepository.save(session);

        return AvailabilitySessionMapper.toResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySessionResponse> getDoctorAvailability(
        UUID clinicId,
        UUID doctorId
    ) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Clinic not found."
            );
        }

        if (!doctorRepository.findByIdAndClinicId(doctorId, clinicId).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Doctor not found in this clinic."
            );
        }

        return availabilitySessionRepository
            .findAllByClinicIdAndDoctorIdOrderByDayOfWeekAscStartTimeAsc(
                clinicId,
                doctorId
            )
            .stream()
            .map(AvailabilitySessionMapper::toResponse)
            .toList();
    }

    public void validateAppointmentAvailability(
        UUID clinicId,
        UUID doctorId,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime
    ) {
        boolean scheduleConfigured =
            availabilitySessionRepository.existsByClinicIdAndDoctorId(
                clinicId,
                doctorId
            );

        // Preserve existing booking behavior for doctors without schedules.
        if (!scheduleConfigured) {
            return;
        }

        List<AvailabilitySession> sessions =
            availabilitySessionRepository
                .findAllByClinicIdAndDoctorIdAndDayOfWeekOrderByStartTimeAsc(
                    clinicId,
                    doctorId,
                    appointmentDate.getDayOfWeek()
                );

        boolean fitsInsideSession = sessions.stream().anyMatch(session ->
            !startTime.isBefore(session.getStartTime())
                && !endTime.isAfter(session.getEndTime())
        );

        if (!fitsInsideSession) {
            throw new AppointmentOutsideAvailabilityException(
                "The appointment must fit entirely within one of the doctor's configured availability sessions."
            );
        }
    }

    public AvailabilitySessionResponse updateSession(
        UUID clinicId,
        UUID doctorId,
        UUID sessionId,
        AvailabilitySessionRequest request
    ) {
        AvailabilitySession session = availabilitySessionRepository
            .findByIdAndClinicId(sessionId, clinicId)
            .orElseThrow(() -> new com.careflow.backend.modules.availability.exception.AvailabilitySessionNotFoundException(
                "Availability session not found in this clinic."
            ));

        if (!session.getDoctor().getId().equals(doctorId)) {
            throw new com.careflow.backend.modules.availability.exception.AvailabilitySessionNotFoundException(
                "Availability session not found for this doctor."
            );
        }

        Doctor doctor = doctorRepository.findByIdAndClinicIdForUpdate(
            doctorId,
            clinicId
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Doctor not found in this clinic."
        ));

        validateTimeRange(request);

        List<AvailabilitySession> existingSessions =
            availabilitySessionRepository
                .findAllByClinicIdAndDoctorIdAndDayOfWeekOrderByStartTimeAsc(
                    clinicId,
                    doctor.getId(),
                    request.dayOfWeek()
                );

        boolean overlaps = existingSessions.stream()
            .filter(existing -> !existing.getId().equals(sessionId))
            .anyMatch(existing ->
                request.startTime().isBefore(existing.getEndTime())
                    && request.endTime().isAfter(existing.getStartTime())
            );

        if (overlaps) {
            throw new AvailabilityConflictException(
                "This availability session overlaps an existing session."
            );
        }

        session.setDayOfWeek(request.dayOfWeek());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());

        return AvailabilitySessionMapper.toResponse(
            availabilitySessionRepository.save(session)
        );
    }
    public void deleteSession(
        UUID clinicId,
        UUID doctorId,
        UUID sessionId
    ) {
        AvailabilitySession session = availabilitySessionRepository
            .findByIdAndClinicId(sessionId, clinicId)
            .orElseThrow(() -> new com.careflow.backend.modules.availability.exception.AvailabilitySessionNotFoundException(
                "Availability session not found in this clinic."
            ));

        if (!session.getDoctor().getId().equals(doctorId)) {
            throw new com.careflow.backend.modules.availability.exception.AvailabilitySessionNotFoundException(
                "Availability session not found for this doctor."
            );
        }

        doctorRepository.findByIdAndClinicIdForUpdate(
            doctorId,
            clinicId
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Doctor not found in this clinic."
        ));

        availabilitySessionRepository.delete(session);
    }
    private void validateTimeRange(AvailabilitySessionRequest request) {
        if (request.startTime() == null || request.endTime() == null
            || request.dayOfWeek() == null) {
            throw new InvalidAvailabilitySessionException(
                "Day of week, start time, and end time are required."
            );
        }

        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidAvailabilitySessionException(
                "Session end time must be after start time."
            );
        }
    }
}