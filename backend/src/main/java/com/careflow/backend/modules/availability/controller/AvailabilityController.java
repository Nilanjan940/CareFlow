package com.careflow.backend.modules.availability.controller;

import com.careflow.backend.modules.availability.dto.AvailabilitySessionRequest;
import com.careflow.backend.modules.availability.dto.AvailabilitySessionResponse;
import com.careflow.backend.modules.availability.service.AvailabilityService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
    "/api/clinics/{clinicId}/doctors/{doctorId}/availability"
)
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(
        AvailabilityService availabilityService
    ) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    public ResponseEntity<AvailabilitySessionResponse> createSession(
        @PathVariable UUID clinicId,
        @PathVariable UUID doctorId,
        @Valid @RequestBody AvailabilitySessionRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                availabilityService.createSession(
                    clinicId,
                    doctorId,
                    request
                )
            );
    }

    @GetMapping
    public ResponseEntity<List<AvailabilitySessionResponse>> getAvailability(
        @PathVariable UUID clinicId,
        @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(
            availabilityService.getDoctorAvailability(
                clinicId,
                doctorId
            )
        );
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<AvailabilitySessionResponse> updateSession(
        @PathVariable UUID clinicId,
        @PathVariable UUID doctorId,
        @PathVariable UUID sessionId,
        @Valid @RequestBody AvailabilitySessionRequest request
    ) {
        return ResponseEntity.ok(
            availabilityService.updateSession(
                clinicId,
                doctorId,
                sessionId,
                request
            )
        );
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(
        @PathVariable UUID clinicId,
        @PathVariable UUID doctorId,
        @PathVariable UUID sessionId
    ) {
        availabilityService.deleteSession(clinicId, doctorId, sessionId);

        return ResponseEntity.noContent().build();
    }
}