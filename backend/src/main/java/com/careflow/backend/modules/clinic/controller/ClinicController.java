package com.careflow.backend.modules.clinic.controller;

import com.careflow.backend.modules.clinic.dto.ClinicRequest;
import com.careflow.backend.modules.clinic.dto.ClinicResponse;
import com.careflow.backend.modules.clinic.service.ClinicService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicResponse registerClinic(
            @Valid @RequestBody ClinicRequest request) {

        return clinicService.registerClinic(request);
    }

    @GetMapping
    public List<ClinicResponse> getAllClinics() {
        return clinicService.getAllClinics();
    }

    @GetMapping("/{id}")
    public ClinicResponse getClinicById(@PathVariable UUID id) {
        return clinicService.getClinicById(id);
    }
}
