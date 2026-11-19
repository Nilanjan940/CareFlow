package com.careflow.backend.modules.patient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String createClinic() throws Exception {

        String email = "patient-test-" + System.nanoTime() + "@careflow.test";

        String response = mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Patient Test Clinic",
                        "email": "%s",
                        "phone": "9876543210",
                        "address": "Kolkata, India"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );
    }

    private String createPatient(
        String clinicId,
        String name,
        String email,
        String phone
    ) throws Exception {

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "%s",
                        "email": "%s",
                        "phone": "%s",
                        "dateOfBirth": "1995-05-20",
                        "gender": "Male",
                        "address": "Kolkata, India"
                    }
                    """.formatted(name, email, phone)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );
    }

    @Test
    void shouldRegisterPatientSuccessfully() throws Exception {

        String clinicId = createClinic();

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Rahul Sharma",
                        "email": "rahul.patient@careflow.test",
                        "phone": "9876543210",
                        "dateOfBirth": "1995-05-20",
                        "gender": "Male",
                        "address": "Kolkata, India"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.clinicName").value("Patient Test Clinic"))
            .andExpect(jsonPath("$.name").value("Rahul Sharma"))
            .andExpect(jsonPath("$.email").value("rahul.patient@careflow.test"))
            .andExpect(jsonPath("$.phone").value("9876543210"))
            .andExpect(jsonPath("$.gender").value("Male"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRetrievePatientById() throws Exception {

        String clinicId = createClinic();

        String patientId = createPatient(
            clinicId,
            "Amit Das",
            "amit.patient@careflow.test",
            "9000000001"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients/{patientId}",
                clinicId,
                patientId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientId))
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.name").value("Amit Das"));
    }

    @Test
    void shouldRetrieveAllPatientsForClinic() throws Exception {

        String clinicId = createClinic();

        createPatient(
            clinicId,
            "Patient One",
            "one@careflow.test",
            "9000000002"
        );

        createPatient(
            clinicId,
            "Patient Two",
            "two@careflow.test",
            "9000000003"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients", clinicId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRejectInvalidPatientData() throws Exception {

        String clinicId = createClinic();

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "",
                        "email": "invalid-email",
                        "phone": "",
                        "gender": "Male"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForMissingClinic() throws Exception {

        String missingClinicId = "00000000-0000-0000-0000-000000000001";

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", missingClinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Test Patient",
                        "phone": "9000000004"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForMissingPatient() throws Exception {

        String clinicId = createClinic();

        String missingPatientId = "00000000-0000-0000-0000-000000000002";

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients/{patientId}",
                clinicId,
                missingPatientId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreventAccessToPatientsFromAnotherClinic() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String patientId = createPatient(
            clinicOne,
            "Private Patient",
            "private@careflow.test",
            "9000000005"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients/{patientId}",
                clinicTwo,
                patientId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldOnlyListPatientsBelongingToRequestedClinic() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        createPatient(
            clinicOne,
            "Clinic One Patient",
            "clinicone@careflow.test",
            "9000000006"
        );

        createPatient(
            clinicTwo,
            "Clinic Two Patient",
            "clinictwo@careflow.test",
            "9000000007"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients", clinicOne))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Clinic One Patient"));
    }

    @Test
    void shouldAllowMultiplePatientsWithSamePhoneNumber() throws Exception {

        String clinicId = createClinic();

        createPatient(
            clinicId,
            "First Patient",
            "first@careflow.test",
            "9000000008"
        );

        createPatient(
            clinicId,
            "Second Patient",
            "second@careflow.test",
            "9000000008"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients", clinicId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldAllowSameContactDetailsAcrossDifferentClinics() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        createPatient(
            clinicOne,
            "Shared Contact Patient One",
            "shared@careflow.test",
            "9000000009"
        );

        createPatient(
            clinicTwo,
            "Shared Contact Patient Two",
            "shared@careflow.test",
            "9000000009"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients", clinicOne))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/patients", clinicTwo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}