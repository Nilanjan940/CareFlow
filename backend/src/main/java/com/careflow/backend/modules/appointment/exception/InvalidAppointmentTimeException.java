package com.careflow.backend.modules.appointment.exception;

public class InvalidAppointmentTimeException extends RuntimeException {

    public InvalidAppointmentTimeException(String message) {
        super(message);
    }
}