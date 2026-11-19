package com.careflow.backend.modules.doctor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record DoctorRequest(

    @NotBlank(message = "Doctor name is required.")
    @Size(max = 150, message = "Doctor name cannot exceed 150 characters.")
    String name,

    @NotBlank(message = "Doctor email is required.")
    @Email(message = "Please provide a valid email address.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    String email,

    @Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    String phone,

    @NotBlank(message = "Specialization is required.")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters.")
    String specialization

) {
    public DoctorRequest {
        if (name != null) {
            name = name.trim();
        }

        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
        }

        if (specialization != null) {
            specialization = specialization.trim();
        }
    }
}