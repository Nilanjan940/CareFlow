package com.careflow.backend;

import com.careflow.backend.modules.clinic.exception.GlobalExceptionHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("local")
@AutoConfigureMockMvc
class CareFlowBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterClinicSuccessfully() throws Exception {

        String uniqueEmail = "test-" + System.nanoTime() + "@careflow.test";

        String requestBody = """
            {
                "name": "CareFlow Automated Test Clinic",
                "email": "%s",
                "phone": "9876543210",
                "address": "Kolkata, India"
            }
            """.formatted(uniqueEmail);

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name")
                .value("CareFlow Automated Test Clinic"))
            .andExpect(jsonPath("$.email").value(uniqueEmail))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectDuplicateClinicEmail() throws Exception {

        String uniqueEmail = "duplicate-" + System.nanoTime() + "@careflow.test";

        String requestBody = """
            {
                "name": "Duplicate Test Clinic",
                "email": "%s",
                "phone": "9876543210",
                "address": "Kolkata, India"
            }
            """.formatted(uniqueEmail);

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message")
                .value("A clinic with this email already exists."));
    }

    @Test
    void shouldRejectInvalidClinicData() throws Exception {

        String requestBody = """
            {
                "name": "",
                "email": "not-an-email",
                "phone": "9876543210",
                "address": "Kolkata, India"
            }
            """;

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.path").value("/api/clinics"));
    }

    @Test
    void shouldReturnNotFoundForMissingClinic() throws Exception {

        mockMvc.perform(get(
                "/api/clinics/00000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Clinic not found."))
            .andExpect(jsonPath("$.path").value(
                "/api/clinics/00000000-0000-0000-0000-000000000001"));
    }

    @Test
    void shouldRetrieveAllClinics() throws Exception {

        mockMvc.perform(get("/api/clinics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldRetrieveClinicById() throws Exception {

        String uniqueEmail = "retrieve-" + System.nanoTime() + "@careflow.test";

        String requestBody = """
            {
                "name": "Clinic Retrieval Test",
                "email": "%s",
                "phone": "9876543210",
                "address": "Kolkata, India"
            }
            """.formatted(uniqueEmail);

        String response = mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String clinicId = response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );

        mockMvc.perform(get("/api/clinics/" + clinicId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(clinicId))
            .andExpect(jsonPath("$.name").value("Clinic Retrieval Test"))
            .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"name\": \"Broken JSON\", "))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Malformed JSON" + " request."))
            .andExpect(jsonPath("$.path").value("/api/clinics"));
    }


    @Test
    void shouldNormalizeClinicEmail() throws Exception {

        String uniqueEmail = "MixedCase-" + System.nanoTime() + "@CareFlow.Test";

        String requestBody = """
            {
                "name": "Email Normalization Test",
                "email": "  %s  ",
                "phone": "9876543210",
                "address": "Kolkata, India"
            }
            """.formatted(uniqueEmail);

        String expectedEmail = uniqueEmail.toLowerCase(java.util.Locale.ROOT);

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value(expectedEmail));
    }

    @Test
    void shouldReturnSafeInternalServerError() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/clinics");

        RuntimeException exception =
            new RuntimeException("Sensitive internal database details");

        var response = handler.handleUnexpectedError(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(
            "An unexpected error occurred.",
            response.getBody().message()
        );
        assertEquals("/api/clinics", response.getBody().path());
    }
}