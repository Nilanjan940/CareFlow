package com.careflow.backend.modules.availability.exception;

public class AvailabilitySessionNotFoundException extends RuntimeException {

    public AvailabilitySessionNotFoundException(String message) {
        super(message);
    }
}