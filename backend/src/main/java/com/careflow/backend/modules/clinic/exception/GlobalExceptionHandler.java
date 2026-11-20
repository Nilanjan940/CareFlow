package com.careflow.backend.modules.clinic.exception;

import com.careflow.backend.modules.clinic.dto.ApiErrorResponse;
import com.careflow.backend.modules.availability.exception.AvailabilityConflictException;
import com.careflow.backend.modules.availability.exception.AvailabilitySessionNotFoundException;
import com.careflow.backend.modules.availability.exception.InvalidAvailabilitySessionException;
import com.careflow.backend.modules.doctor.exception.DoctorNotFoundException;
import com.careflow.backend.modules.doctor.exception.DuplicateDoctorException;
import com.careflow.backend.modules.patient.exception.PatientNotFoundException;
import com.careflow.backend.modules.appointment.exception.AppointmentConflictException;
import com.careflow.backend.modules.appointment.exception.AppointmentOutsideAvailabilityException;
import com.careflow.backend.modules.appointment.exception.AppointmentNotFoundException;
import com.careflow.backend.modules.appointment.exception.AppointmentUpdateNotAllowedException;
import com.careflow.backend.modules.appointment.exception.InvalidAppointmentTimeException;
import com.careflow.backend.modules.appointment.exception.InvalidAppointmentStatusException;

import com.careflow.backend.modules.queue.exception.QueueEntryNotFoundException;
import com.careflow.backend.modules.queue.exception.InvalidQueueStatusException;
import com.careflow.backend.modules.queue.exception.InvalidQueueCheckInException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClinicNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleClinicNotFound(
            ClinicNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDoctorNotFound(
            DoctorNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePatientNotFound(
            PatientNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAppointmentNotFound(
            AppointmentNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateClinicException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateClinic(
            DuplicateClinicException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(DuplicateDoctorException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateDoctor(
            DuplicateDoctorException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleAppointmentConflict(
            AppointmentConflictException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request.getRequestURI()
        );
    }


    @ExceptionHandler(AvailabilityConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleAvailabilityConflict(
            AvailabilityConflictException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(AvailabilitySessionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAvailabilitySessionNotFound(
            AvailabilitySessionNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidAvailabilitySessionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAvailabilitySession(
            InvalidAvailabilitySessionException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }
    @ExceptionHandler(AppointmentOutsideAvailabilityException.class)
    public ResponseEntity<ApiErrorResponse> handleAppointmentOutsideAvailability(
            AppointmentOutsideAvailabilityException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidAppointmentTimeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAppointmentTime(
            InvalidAppointmentTimeException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }


    @ExceptionHandler(InvalidAppointmentStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAppointmentStatus(
            InvalidAppointmentStatusException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(AppointmentUpdateNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleAppointmentUpdateNotAllowed(
            AppointmentUpdateNotAllowedException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }
    @ExceptionHandler(QueueEntryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleQueueEntryNotFound(
            QueueEntryNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidQueueStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQueueStatus(
            InvalidQueueStatusException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidQueueCheckInException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidQueueCheckIn(
            InvalidQueueCheckInException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            exception.getMessage(),
            request.getRequestURI()
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Invalid request data.");

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            message,
            request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "Malformed JSON request.",
            request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedError(
            Exception exception,
            HttpServletRequest request) {

        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred.",
            request.getRequestURI()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            String path) {

        ApiErrorResponse response = new ApiErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            path
        );

        return ResponseEntity.status(status).body(response);
    }
}