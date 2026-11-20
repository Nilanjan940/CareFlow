package com.careflow.backend.modules.appointment.exception;

public class InvalidAppointmentStatusException extends RuntimeException {

    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
}