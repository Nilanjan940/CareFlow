package com.careflow.backend.modules.appointment.exception;

public class AppointmentOutsideAvailabilityException extends RuntimeException {

    public AppointmentOutsideAvailabilityException(String message) {
        super(message);
    }
}