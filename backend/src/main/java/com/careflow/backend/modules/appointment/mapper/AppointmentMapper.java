package com.careflow.backend.modules.appointment.mapper;

import com.careflow.backend.modules.appointment.dto.AppointmentResponse;
import com.careflow.backend.modules.appointment.entity.Appointment;

public class AppointmentMapper {

    private AppointmentMapper() {
    }

    public static AppointmentResponse toResponse(
        Appointment appointment
    ) {

        return new AppointmentResponse(
            appointment.getId(),

            appointment.getClinic().getId(),
            appointment.getClinic().getName(),

            appointment.getDoctor().getId(),
            appointment.getDoctor().getName(),

            appointment.getPatient().getId(),
            appointment.getPatient().getName(),

            appointment.getAppointmentDate(),
            appointment.getStartTime(),
            appointment.getEndTime(),

            appointment.getReason(),
            appointment.getStatus(),
            appointment.getNotes(),

            appointment.getCreatedAt()
        );
    }
}