package com.careflow.backend.modules.appointment.service;

import com.careflow.backend.modules.appointment.dto.AppointmentHistoryResponse;
import com.careflow.backend.modules.appointment.entity.Appointment;
import com.careflow.backend.modules.appointment.entity.AppointmentHistory;
import com.careflow.backend.modules.appointment.entity.AppointmentHistoryAction;
import com.careflow.backend.modules.appointment.exception.AppointmentNotFoundException;
import com.careflow.backend.modules.appointment.repository.AppointmentHistoryRepository;
import com.careflow.backend.modules.appointment.repository.AppointmentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AppointmentHistoryService {

    private final AppointmentHistoryRepository historyRepository;
    private final AppointmentRepository appointmentRepository;

    public AppointmentHistoryService(
        AppointmentHistoryRepository historyRepository,
        AppointmentRepository appointmentRepository
    ) {
        this.historyRepository = historyRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public void recordCreated(Appointment appointment) {

        record(
            appointment,
            AppointmentHistoryAction.CREATED,
            "Appointment created.",
            null,
            createSnapshot(appointment)
        );
    }

    public void recordUpdated(
        Appointment appointment,
        Map<String, Object> oldValues
    ) {

        Map<String, Object> newValues = createSnapshot(appointment);

        if (oldValues.equals(newValues)) {
            return;
        }

        record(
            appointment,
            AppointmentHistoryAction.UPDATED,
            "Appointment details updated.",
            oldValues,
            newValues
        );
    }

    public void recordRescheduled(
        Appointment appointment,
        Map<String, Object> oldValues
    ) {

        Map<String, Object> newValues = createSnapshot(appointment);

        if (oldValues.equals(newValues)) {
            return;
        }

        record(
            appointment,
            AppointmentHistoryAction.RESCHEDULED,
            "Appointment rescheduled.",
            oldValues,
            newValues
        );
    }

    public void recordCancelled(
        Appointment appointment,
        String oldStatus,
        String oldCancellationReason
    ) {

        Map<String, Object> oldValues = new LinkedHashMap<>();
        oldValues.put("status", oldStatus);
        oldValues.put("cancellationReason", oldCancellationReason);

        Map<String, Object> newValues = new LinkedHashMap<>();
        newValues.put("status", appointment.getStatus().name());
        newValues.put(
            "cancellationReason",
            appointment.getCancellationReason()
        );

        record(
            appointment,
            AppointmentHistoryAction.CANCELLED,
            "Appointment cancelled.",
            oldValues,
            newValues
        );
    }

    public void recordStatusChanged(
        Appointment appointment,
        String oldStatus,
        String newStatus
    ) {

        Map<String, Object> oldValues = Map.of(
            "status", oldStatus
        );

        Map<String, Object> newValues = Map.of(
            "status", newStatus
        );

        record(
            appointment,
            AppointmentHistoryAction.STATUS_CHANGED,
            "Appointment status changed from " +
                oldStatus + " to " + newStatus + ".",
            oldValues,
            newValues
        );
    }

    public Map<String, Object> createSnapshot(Appointment appointment) {

        Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("doctorId", appointment.getDoctor().getId().toString());
        snapshot.put("patientId", appointment.getPatient().getId().toString());
        snapshot.put("appointmentDate", appointment.getAppointmentDate().toString());
        snapshot.put("startTime", appointment.getStartTime().toString());
        snapshot.put("endTime", appointment.getEndTime().toString());
        snapshot.put("reason", appointment.getReason());
        snapshot.put("notes", appointment.getNotes());
        snapshot.put("status", appointment.getStatus().name());

        return snapshot;
    }

    @Transactional(readOnly = true)
    public List<AppointmentHistoryResponse> getAppointmentHistory(
        UUID clinicId,
        UUID appointmentId
    ) {

        appointmentRepository.findByIdAndClinicId(
            appointmentId,
            clinicId
        ).orElseThrow(() -> new AppointmentNotFoundException(
            "Appointment not found with ID: " + appointmentId
        ));

        return historyRepository
            .findAllByAppointment_IdAndClinic_IdOrderByCreatedAtDesc(
                appointmentId,
                clinicId
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private AppointmentHistoryResponse toResponse(
        AppointmentHistory history
    ) {

        return new AppointmentHistoryResponse(
            history.getId(),
            history.getAction(),
            history.getDescription(),
            history.getOldValues(),
            history.getNewValues(),
            history.getCreatedAt()
        );
    }

    private void record(
        Appointment appointment,
        AppointmentHistoryAction action,
        String description,
        Map<String, Object> oldValues,
        Map<String, Object> newValues
    ) {

        AppointmentHistory history = new AppointmentHistory();

        history.setClinic(appointment.getClinic());
        history.setAppointment(appointment);
        history.setAction(action);
        history.setDescription(description);
        history.setOldValues(oldValues);
        history.setNewValues(newValues);

        historyRepository.save(history);
    }
}