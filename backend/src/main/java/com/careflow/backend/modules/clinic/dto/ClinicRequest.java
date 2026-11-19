package com.careflow.backend.modules.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClinicRequest(
    @NotBlank(message = "Clinic name is required.")
    @Size(max = 150, message = "Clinic name cannot exceed 150 characters.")
    String name,

    @NotBlank(message = "Clinic email is required.")
    @Email(message = "Please provide a valid email address.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    String email,

    @Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    String phone,

    @Size(max = 500, message = "Address cannot exceed 500 characters.")
    String address
) {
    public ClinicRequest {
        if (name != null) {
            name = name.trim();
        }

        if (email != null) {
            email = email.trim().toLowerCase(java.util.Locale.ROOT);
        }
    }
}