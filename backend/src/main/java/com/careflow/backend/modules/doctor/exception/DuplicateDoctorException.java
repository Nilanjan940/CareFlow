package com.careflow.backend.modules.doctor.exception;

public class DuplicateDoctorException extends RuntimeException {

    public DuplicateDoctorException(String message) {
        super(message);
    }
}