package com.careflow.backend.modules.appointment.exception;

public class AppointmentUpdateNotAllowedException extends RuntimeException {

    public AppointmentUpdateNotAllowedException(String message) {
        super(message);
    }
}