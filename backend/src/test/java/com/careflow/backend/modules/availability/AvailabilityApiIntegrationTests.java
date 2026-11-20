package com.careflow.backend.modules.availability;

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
class AvailabilityApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String createClinic() throws Exception {

        String email = "availability-test-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Availability Test Clinic",
                        "email": "%s",
                        "phone": "9876543210",
                        "address": "Kolkata, India"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return extractId(response);
    }

    private String createDoctor(String clinicId) throws Exception {

        String email = "availability-doctor-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/doctors", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Dr. Availability Test",
                        "email": "%s",
                        "phone": "9876543210",
                        "specialization": "Cardiology"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return extractId(response);
    }

    private String createSessionRequest(
        String day,
        String startTime,
        String endTime
    ) {
        return """
            {
                "dayOfWeek": "%s",
                "startTime": "%s",
                "endTime": "%s"
            }
            """.formatted(day, startTime, endTime);
    }

    private String createSession(
        String clinicId,
        String doctorId,
        String day,
        String startTime,
        String endTime
    ) throws Exception {

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability",
                clinicId,
                doctorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSessionRequest(day, startTime, endTime)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return extractId(response);
    }

    private String extractId(String response) {

        return response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );
    }

    @Test
    void shouldCreateAndRetrieveAvailabilitySession() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);

        createSession(
            clinicId,
            doctorId,
            "MONDAY",
            "09:00:00",
            "12:00:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability",
                clinicId,
                doctorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
            .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
            .andExpect(jsonPath("$[0].endTime").value("12:00:00"));
    }

    @Test
    void shouldUpdateAndDeleteAvailabilitySession() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);

        String sessionId = createSession(
            clinicId,
            doctorId,
            "MONDAY",
            "09:00:00",
            "12:00:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability/{sessionId}",
                clinicId,
                doctorId,
                sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSessionRequest(
                    "TUESDAY",
                    "10:00:00",
                    "13:00:00"
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dayOfWeek").value("TUESDAY"))
            .andExpect(jsonPath("$.startTime").value("10:00:00"))
            .andExpect(jsonPath("$.endTime").value("13:00:00"));

        mockMvc.perform(delete(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability/{sessionId}",
                clinicId,
                doctorId,
                sessionId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability",
                clinicId,
                doctorId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRejectOverlappingAvailabilitySessions() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);

        createSession(
            clinicId,
            doctorId,
            "WEDNESDAY",
            "09:00:00",
            "12:00:00"
        );

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability",
                clinicId,
                doctorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSessionRequest(
                    "WEDNESDAY",
                    "11:00:00",
                    "13:00:00"
                )))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowAdjacentAvailabilitySessions() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);

        createSession(
            clinicId,
            doctorId,
            "THURSDAY",
            "09:00:00",
            "12:00:00"
        );

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/doctors/{doctorId}/availability",
                clinicId,
                doctorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createSessionRequest(
                    "THURSDAY",
                    "12:00:00",
                    "14:00:00"
                )))
            .andExpect(status().isCreated());
    }

    private String createPatient(String clinicId) throws Exception {
        String email = "availability-patient-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Availability Test Patient",
                        "email": "%s",
                        "phone": "9000000001",
                        "dateOfBirth": "1995-05-20",
                        "gender": "Male",
                        "address": "Kolkata, India"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return extractId(response);
    }

    private String appointmentRequest(
        String doctorId,
        String patientId,
        String date,
        String startTime,
        String endTime
    ) {
        return """
            {
                "doctorId": "%s",
                "patientId": "%s",
                "appointmentDate": "%s",
                "startTime": "%s",
                "endTime": "%s",
                "reason": "Regular consultation",
                "notes": "Availability integration test"
            }
            """.formatted(doctorId, patientId, date, startTime, endTime);
    }

    @Test
    void shouldAllowAppointmentWithinAvailabilitySession() throws Exception {
        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        createSession(clinicId, doctorId, "THURSDAY", "09:00:00", "13:00:00");

        mockMvc.perform(post("/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequest(
                    doctorId, patientId, "2027-05-20", "10:00:00", "10:30:00"
                )))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectAppointmentOutsideAvailabilitySession() throws Exception {
        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        createSession(clinicId, doctorId, "THURSDAY", "09:00:00", "13:00:00");

        mockMvc.perform(post("/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequest(
                    doctorId, patientId, "2027-05-20", "14:00:00", "14:30:00"
                )))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldAllowAppointmentWhenDoctorHasNoAvailabilitySchedule()
            throws Exception {
        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        mockMvc.perform(post("/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequest(
                    doctorId, patientId, "2027-05-20", "14:00:00", "14:30:00"
                )))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectReschedulingOutsideAvailabilitySession()
            throws Exception {
        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        createSession(clinicId, doctorId, "THURSDAY", "09:00:00", "13:00:00");

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequest(
                    doctorId, patientId, "2027-05-20", "10:00:00", "10:30:00"
                )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String appointmentId = extractId(response);

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId, appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(appointmentRequest(
                    doctorId, patientId, "2027-05-20", "14:00:00", "14:30:00"
                )))
            .andExpect(status().isConflict());
    }
}