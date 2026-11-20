package com.careflow.backend.modules.appointment.controller;

import com.careflow.backend.modules.appointment.dto.AppointmentRequest;
import com.careflow.backend.modules.appointment.dto.AppointmentResponse;
import com.careflow.backend.modules.appointment.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics/{clinicId}/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
        AppointmentService appointmentService
    ) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
        @PathVariable UUID clinicId,
        @Valid @RequestBody AppointmentRequest request
    ) {

        AppointmentResponse response =
            appointmentService.createAppointment(clinicId, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
        @PathVariable UUID clinicId
    ) {

        return ResponseEntity.ok(
            appointmentService.getAllAppointments(clinicId)
        );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
        @PathVariable UUID clinicId,
        @PathVariable UUID appointmentId
    ) {

        return ResponseEntity.ok(
            appointmentService.getAppointmentById(
                clinicId,
                appointmentId
            )
        );
    }
}