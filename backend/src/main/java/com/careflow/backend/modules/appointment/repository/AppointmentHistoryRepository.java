package com.careflow.backend.modules.appointment.repository;

import com.careflow.backend.modules.appointment.entity.AppointmentHistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentHistoryRepository
    extends JpaRepository<AppointmentHistory, UUID> {

    List<AppointmentHistory>
        findAllByAppointment_IdAndClinic_IdOrderByCreatedAtDesc(
            UUID appointmentId,
            UUID clinicId
        );

}