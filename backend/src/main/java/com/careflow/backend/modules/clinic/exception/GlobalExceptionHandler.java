package com.careflow.backend.modules.clinic.exception;

import com.careflow.backend.modules.clinic.dto.ApiErrorResponse;

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