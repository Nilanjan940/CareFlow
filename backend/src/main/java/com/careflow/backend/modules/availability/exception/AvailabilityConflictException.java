package com.careflow.backend.modules.availability.exception;

public class AvailabilityConflictException extends RuntimeException {

    public AvailabilityConflictException(String message) {
        super(message);
    }
}