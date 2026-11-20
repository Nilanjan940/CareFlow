package com.careflow.backend.modules.queue;

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
class QueueApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String createClinic() throws Exception {

        String email = "queue-test-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Queue Test Clinic",
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

        String email = "queue-doctor-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/doctors", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Dr. Queue Test",
                        "email": "%s",
                        "phone": "9876543210",
                        "specialization": "General Medicine"
                    }
                    """.formatted(email)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return extractId(response);
    }

    private String createPatient(String clinicId) throws Exception {

        String phone = "9" + String.format("%09d", Math.floorMod(System.nanoTime(), 1000000000L));
        String email = "queue-patient-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Queue Test Patient",
                        "email": "%s",
                        "phone": "%s",
                        "dateOfBirth": "1995-05-20",
                        "gender": "Male",
                        "address": "Kolkata, India"
                    }
                    """.formatted(email, phone)))
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
    void shouldCheckInWalkInPatientSuccessfully() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20",
                        "notes": "Walk-in consultation"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.doctorId").value(doctorId))
            .andExpect(jsonPath("$.patientId").value(patientId))
            .andExpect(jsonPath("$.queueDate").value("2027-05-20"))
            .andExpect(jsonPath("$.queueNumber").value(1))
            .andExpect(jsonPath("$.status").value("WAITING"))
            .andExpect(jsonPath("$.notes").value("Walk-in consultation"))
            .andExpect(jsonPath("$.checkedInAt").exists());
    }

    @Test
    void shouldAssignSequentialQueueNumbersForSameDoctorAndDate()
            throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String firstPatientId = createPatient(clinicId);
        String secondPatientId = createPatient(clinicId);

        String firstRequest = """
            {
                "doctorId": "%s",
                "patientId": "%s",
                "queueDate": "2027-05-20"
            }
            """.formatted(doctorId, firstPatientId);

        String secondRequest = """
            {
                "doctorId": "%s",
                "patientId": "%s",
                "queueDate": "2027-05-20"
            }
            """.formatted(doctorId, secondPatientId);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.queueNumber").value(1));

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.queueNumber").value(2));
    }

    @Test
    void shouldResetQueueNumberForNewDate() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String firstPatientId = createPatient(clinicId);
        String secondPatientId = createPatient(clinicId);

        String firstRequest = """
            {
                "doctorId": "%s",
                "patientId": "%s",
                "queueDate": "2027-05-20"
            }
            """.formatted(doctorId, firstPatientId);

        String secondRequest = """
            {
                "doctorId": "%s",
                "patientId": "%s",
                "queueDate": "2027-05-21"
            }
            """.formatted(doctorId, secondPatientId);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.queueNumber").value(1));

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequest))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.queueNumber").value(1));
    }

    @Test
    void shouldCheckInExistingAppointmentSuccessfully() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "appointmentDate": "2027-05-20",
                        "startTime": "10:00:00",
                        "endTime": "10:30:00",
                        "reason": "Regular consultation",
                        "notes": "Queue integration test"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String appointmentId = extractId(appointmentResponse);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "appointmentId": "%s"
                    }
                    """.formatted(appointmentId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.appointmentId").value(appointmentId))
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.doctorId").value(doctorId))
            .andExpect(jsonPath("$.patientId").value(patientId))
            .andExpect(jsonPath("$.queueDate").value("2027-05-20"))
            .andExpect(jsonPath("$.queueNumber").value(1))
            .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldRejectDuplicateAppointmentCheckIn() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "appointmentDate": "2027-05-20",
                        "startTime": "11:00:00",
                        "endTime": "11:30:00",
                        "reason": "Regular consultation"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String appointmentId = extractId(appointmentResponse);

        String checkInRequest = """
            {
                "appointmentId": "%s"
            }
            """.formatted(appointmentId);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkInRequest))
            .andExpect(status().isCreated());

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkInRequest))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCompleteQueueEntrySuccessfully() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String checkInResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("WAITING"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String queueEntryId = extractId(checkInResponse);

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "IN_PROGRESS"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.startedAt").exists())
            .andExpect(jsonPath("$.completedAt").doesNotExist());

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "COMPLETED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.startedAt").exists())
            .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void shouldSkipAndRecallQueueEntrySuccessfully() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String checkInResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("WAITING"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String queueEntryId = extractId(checkInResponse);

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "SKIPPED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SKIPPED"));

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "WAITING"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("WAITING"))
            .andExpect(jsonPath("$.queueNumber").value(1))
            .andExpect(jsonPath("$.startedAt").doesNotExist())
            .andExpect(jsonPath("$.completedAt").doesNotExist());
    }

    @Test
    void shouldRejectInvalidQueueStatusTransitions() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String checkInResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String queueEntryId = extractId(checkInResponse);

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "COMPLETED"
                    }
                    """))
            .andExpect(status().isBadRequest());

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "IN_PROGRESS"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "COMPLETED"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, queueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "WAITING"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRetrieveDailyQueueWithStatusFilter() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);

        String firstPatientId = createPatient(clinicId);
        String secondPatientId = createPatient(clinicId);
        String thirdPatientId = createPatient(clinicId);

        String firstResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, firstPatientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String firstQueueEntryId = extractId(firstResponse);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, secondPatientId)))
            .andExpect(status().isCreated());

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, thirdPatientId)))
            .andExpect(status().isCreated());

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/queue/{queueEntryId}/status",
                clinicId, firstQueueEntryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "IN_PROGRESS"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/clinics/{clinicId}/queue", clinicId)
                .param("doctorId", doctorId)
                .param("date", "2027-05-20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].queueNumber").value(1))
            .andExpect(jsonPath("$.content[1].queueNumber").value(2))
            .andExpect(jsonPath("$.content[2].queueNumber").value(3));

        mockMvc.perform(get("/api/clinics/{clinicId}/queue", clinicId)
                .param("doctorId", doctorId)
                .param("date", "2027-05-20")
                .param("status", "WAITING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].status").value("WAITING"))
            .andExpect(jsonPath("$.content[1].status").value("WAITING"));

        mockMvc.perform(get("/api/clinics/{clinicId}/queue", clinicId)
                .param("doctorId", doctorId)
                .param("date", "2027-05-20")
                .param("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].status").value("IN_PROGRESS"));
    }

    @Test
    void shouldPaginateDailyQueueResults() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);

        for (int i = 0; i < 3; i++) {
            String patientId = createPatient(clinicId);

            mockMvc.perform(post(
                    "/api/clinics/{clinicId}/queue", clinicId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "doctorId": "%s",
                            "patientId": "%s",
                            "queueDate": "2027-05-20"
                        }
                        """.formatted(doctorId, patientId)))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/clinics/{clinicId}/queue", clinicId)
                .param("doctorId", doctorId)
                .param("date", "2027-05-20")
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].queueNumber").value(1))
            .andExpect(jsonPath("$.content[1].queueNumber").value(2))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/clinics/{clinicId}/queue", clinicId)
                .param("doctorId", doctorId)
                .param("date", "2027-05-20")
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].queueNumber").value(3))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void shouldPreventCrossClinicQueueAccess() throws Exception {

        String firstClinicId = createClinic();
        String secondClinicId = createClinic();

        String doctorId = createDoctor(firstClinicId);
        String patientId = createPatient(firstClinicId);

        String checkInResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", firstClinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "queueDate": "2027-05-20"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String queueEntryId = extractId(checkInResponse);

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/queue/{queueEntryId}",
                secondClinicId, queueEntryId))
            .andExpect(status().isNotFound());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/queue", secondClinicId)
                .param("doctorId", doctorId)
                .param("date", "2027-05-20"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectCheckInForCancelledAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "appointmentDate": "2027-05-20",
                        "startTime": "12:00:00",
                        "endTime": "12:30:00",
                        "reason": "Queue validation test"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String appointmentId = extractId(appointmentResponse);

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId, appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "CANCELLED",
                        "cancellationReason": "Patient unavailable"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "appointmentId": "%s"
                    }
                    """.formatted(appointmentId)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCheckInWhenPatientDoesNotMatchAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);
        String differentPatientId = createPatient(clinicId);

        String appointmentResponse = mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "%s",
                        "patientId": "%s",
                        "appointmentDate": "2027-05-20",
                        "startTime": "13:00:00",
                        "endTime": "13:30:00",
                        "reason": "Queue validation test"
                    }
                    """.formatted(doctorId, patientId)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String appointmentId = extractId(appointmentResponse);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/queue", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "appointmentId": "%s",
                        "patientId": "%s"
                    }
                    """.formatted(appointmentId, differentPatientId)))
            .andExpect(status().isBadRequest());
    }
}