package com.careflow.backend.modules.doctor.controller;

import com.careflow.backend.modules.doctor.dto.DoctorRequest;
import com.careflow.backend.modules.doctor.dto.DoctorResponse;
import com.careflow.backend.modules.doctor.service.DoctorService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics/{clinicId}/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> registerDoctor(
        @PathVariable UUID clinicId,
        @Valid @RequestBody DoctorRequest request
    ) {
        DoctorResponse response =
            doctorService.registerDoctor(clinicId, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(
        @PathVariable UUID clinicId
    ) {
        return ResponseEntity.ok(
            doctorService.getAllDoctors(clinicId)
        );
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<DoctorResponse> getDoctorById(
        @PathVariable UUID clinicId,
        @PathVariable UUID doctorId
    ) {
        return ResponseEntity.ok(
            doctorService.getDoctorById(clinicId, doctorId)
        );
    }
}