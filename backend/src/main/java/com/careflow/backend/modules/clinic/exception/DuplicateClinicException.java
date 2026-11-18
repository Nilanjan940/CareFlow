package com.careflow.backend.modules.clinic.exception;

public class DuplicateClinicException extends RuntimeException {

    public DuplicateClinicException(String message) {
        super(message);
    }
}
