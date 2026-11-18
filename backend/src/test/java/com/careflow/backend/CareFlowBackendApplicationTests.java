
package com.careflow.backend;
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

        // First registration should succeed.
        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated());

        // Second registration with the same email should fail.
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
        .andExpect(jsonPath("$.message").exists())
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
}

