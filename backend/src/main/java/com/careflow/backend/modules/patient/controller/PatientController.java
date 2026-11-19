package com.careflow.backend.modules.patient.controller;

import com.careflow.backend.modules.patient.dto.PatientRequest;
import com.careflow.backend.modules.patient.dto.PatientResponse;
import com.careflow.backend.modules.patient.service.PatientService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics/{clinicId}/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(
        @PathVariable UUID clinicId,
        @Valid @RequestBody PatientRequest request
    ) {
        PatientResponse response =
            patientService.registerPatient(clinicId, request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients(
        @PathVariable UUID clinicId
    ) {
        return ResponseEntity.ok(
            patientService.getAllPatients(clinicId)
        );
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(
        @PathVariable UUID clinicId,
        @PathVariable UUID patientId
    ) {
        return ResponseEntity.ok(
            patientService.getPatientById(clinicId, patientId)
        );
    }
}