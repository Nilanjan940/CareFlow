package com.careflow.backend.modules.appointment.controller;

import com.careflow.backend.modules.appointment.dto.AppointmentPageResponse;
import com.careflow.backend.modules.appointment.dto.AppointmentRequest;
import com.careflow.backend.modules.appointment.dto.AppointmentResponse;
import com.careflow.backend.modules.appointment.dto.AppointmentStatusUpdateRequest;
import com.careflow.backend.modules.appointment.entity.AppointmentStatus;
import com.careflow.backend.modules.appointment.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<AppointmentPageResponse> getAllAppointments(
        @PathVariable UUID clinicId,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(required = false) UUID doctorId,
        @RequestParam(required = false) UUID patientId,
        @PageableDefault(
            page = 0,
            size = 10,
            sort = {"appointmentDate", "startTime"},
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {

        Page<AppointmentResponse> result =
            appointmentService.searchAppointments(
                clinicId,
                date,
                status,
                doctorId,
                patientId,
                pageable
            );

        AppointmentPageResponse response = new AppointmentPageResponse(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );

        return ResponseEntity.ok(response);
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

    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
        @PathVariable UUID clinicId,
        @PathVariable UUID appointmentId,
        @Valid @RequestBody AppointmentRequest request
    ) {

        return ResponseEntity.ok(
            appointmentService.updateAppointment(
                clinicId,
                appointmentId,
                request
            )
        );
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
        @PathVariable UUID clinicId,
        @PathVariable UUID appointmentId,
        @Valid @RequestBody AppointmentStatusUpdateRequest request
    ) {

        return ResponseEntity.ok(
            appointmentService.updateAppointmentStatus(
                clinicId,
                appointmentId,
                request
            )
        );
    }
}