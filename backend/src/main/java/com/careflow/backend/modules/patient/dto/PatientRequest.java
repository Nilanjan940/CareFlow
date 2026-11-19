package com.careflow.backend.modules.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Locale;

public record PatientRequest(

    @NotBlank(message = "Patient name is required.")
    @Size(max = 150, message = "Patient name cannot exceed 150 characters.")
    String name,

    @Email(message = "Please provide a valid email address.")
    @Size(max = 150, message = "Email cannot exceed 150 characters.")
    String email,

    @NotBlank(message = "Phone number is required.")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    String phone,

    @PastOrPresent(message = "Date of birth cannot be in the future.")
    LocalDate dateOfBirth,

    @Size(max = 20, message = "Gender cannot exceed 20 characters.")
    String gender,

    @Size(max = 500, message = "Address cannot exceed 500 characters.")
    String address

) {
    public PatientRequest {
        if (name != null) {
            name = name.trim();
        }

        if (email != null) {
            email = email.trim().toLowerCase(Locale.ROOT);
            if (email.isEmpty()) {
                email = null;
            }
        }

        if (phone != null) {
            phone = phone.trim();
        }

        if (gender != null) {
            gender = gender.trim();
            if (gender.isEmpty()) {
                gender = null;
            }
        }

        if (address != null) {
            address = address.trim();
            if (address.isEmpty()) {
                address = null;
            }
        }
    }
}