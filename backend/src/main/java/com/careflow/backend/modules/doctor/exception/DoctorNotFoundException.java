package com.careflow.backend.modules.doctor.exception;

public class DoctorNotFoundException extends RuntimeException {

    public DoctorNotFoundException(String message) {
        super(message);
    }
}