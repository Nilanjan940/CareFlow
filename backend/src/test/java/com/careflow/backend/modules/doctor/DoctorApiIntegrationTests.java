package com.careflow.backend.modules.doctor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("local")
@AutoConfigureMockMvc
class DoctorApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String createClinic() throws Exception {

        String email = "doctor-test-" + System.nanoTime() + "@careflow.test";

        String requestBody = """
            {
                "name": "Doctor Test Clinic",
                "email": "%s",
                "phone": "9876543210",
                "address": "Kolkata, India"
            }
            """.formatted(email);

        String response = mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
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
    void shouldRegisterDoctorSuccessfully() throws Exception {

        String clinicId = createClinic();

        String requestBody = """
            {
                "name": "Dr. Amit Sharma",
                "email": "amit.sharma@careflow.test",
                "phone": "9876543210",
                "specialization": "Cardiology"
            }
            """;

        mockMvc.perform(post("/api/clinics/" + clinicId + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.clinicName").value("Doctor Test Clinic"))
            .andExpect(jsonPath("$.name").value("Dr. Amit Sharma"))
            .andExpect(jsonPath("$.email").value("amit.sharma@careflow.test"))
            .andExpect(jsonPath("$.specialization").value("Cardiology"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRetrieveDoctorById() throws Exception {

        String clinicId = createClinic();

        String requestBody = """
            {
                "name": "Dr. Priya Das",
                "email": "priya.das@careflow.test",
                "phone": "9876543211",
                "specialization": "Dermatology"
            }
            """;

        String response = mockMvc.perform(
                post("/api/clinics/" + clinicId + "/doctors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String doctorId = response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );

        mockMvc.perform(get(
                "/api/clinics/" + clinicId + "/doctors/" + doctorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(doctorId))
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.name").value("Dr. Priya Das"))
            .andExpect(jsonPath("$.email").value("priya.das@careflow.test"))
            .andExpect(jsonPath("$.specialization").value("Dermatology"));
    }

    @Test
    void shouldRetrieveAllDoctorsForClinic() throws Exception {

        String clinicId = createClinic();

        String requestBody = """
            {
                "name": "Dr. Rahul Sen",
                "email": "rahul.sen@careflow.test",
                "phone": "9876543212",
                "specialization": "Neurology"
            }
            """;

        mockMvc.perform(post("/api/clinics/" + clinicId + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/clinics/" + clinicId + "/doctors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Dr. Rahul Sen"))
            .andExpect(jsonPath("$[0].specialization").value("Neurology"));
    }

        @Test
    void shouldRejectDuplicateDoctorEmailWithinSameClinic() throws Exception {

        String clinicId = createClinic();

        String requestBody = """
            {
                "name": "Dr. Duplicate Test",
                "email": "duplicate.doctor@careflow.test",
                "phone": "9876543210",
                "specialization": "Cardiology"
            }
            """;

        mockMvc.perform(post("/api/clinics/" + clinicId + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clinics/" + clinicId + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message")
                .value("A doctor with this email already exists in this clinic."));
    }

        @Test
    void shouldRejectInvalidDoctorData() throws Exception {

        String clinicId = createClinic();

        String requestBody = """
            {
                "name": "",
                "email": "invalid-email",
                "phone": "9876543210",
                "specialization": ""
            }
            """;

        mockMvc.perform(post("/api/clinics/" + clinicId + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.path")
                .value("/api/clinics/" + clinicId + "/doctors"));
    }

        @Test
    void shouldReturnNotFoundForMissingClinic() throws Exception {

        String missingClinicId = "00000000-0000-0000-0000-000000000001";

        String requestBody = """
            {
                "name": "Dr. Missing Clinic",
                "email": "missing.clinic@careflow.test",
                "phone": "9876543210",
                "specialization": "General Medicine"
            }
            """;

        mockMvc.perform(post(
                "/api/clinics/" + missingClinicId + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldReturnNotFoundForMissingDoctor() throws Exception {

        String clinicId = createClinic();

        String missingDoctorId = "00000000-0000-0000-0000-000000000002";

        mockMvc.perform(get(
                "/api/clinics/" + clinicId + "/doctors/" + missingDoctorId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

        @Test
    void shouldPreventAccessToDoctorsFromAnotherClinic() throws Exception {

        String clinicA = createClinic();
        String clinicB = createClinic();

        String requestBody = """
            {
                "name": "Dr. Confidential",
                "email": "confidential@careflow.test",
                "phone": "9876543210",
                "specialization": "Cardiology"
            }
            """;

        String response = mockMvc.perform(
                post("/api/clinics/" + clinicA + "/doctors")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String doctorId = response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );

        mockMvc.perform(get(
                "/api/clinics/" + clinicB + "/doctors/" + doctorId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

        @Test
    void shouldOnlyListDoctorsBelongingToRequestedClinic() throws Exception {

        String clinicA = createClinic();
        String clinicB = createClinic();

        String requestBody = """
            {
                "name": "Dr. Clinic A",
                "email": "clinic.a.doctor@careflow.test",
                "phone": "9876543210",
                "specialization": "Cardiology"
            }
            """;

        mockMvc.perform(post("/api/clinics/" + clinicA + "/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/clinics/" + clinicB + "/doctors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }
}