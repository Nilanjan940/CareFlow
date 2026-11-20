package com.careflow.backend.modules.appointment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private String createClinic() throws Exception {

        String email = "appointment-test-" + System.nanoTime()
            + "@careflow.test";

        String response = mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Appointment Test Clinic",
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

        String email = "doctor-" + System.nanoTime() + "@careflow.test";

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/doctors", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Dr. Appointment Test",
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

    private String createPatient(String clinicId) throws Exception {

        String email = "patient-" + System.nanoTime() + "@careflow.test";

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/patients", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Appointment Test Patient",
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

    private String extractId(String response) {

        return response.replaceAll(
            "(?s).*\"id\"\\s*:\\s*\"([^\"]+)\".*",
            "$1"
        );
    }

    private String createAppointmentRequest(
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
                "notes": "Integration test appointment"
            }
            """.formatted(
                doctorId,
                patientId,
                date,
                startTime,
                endTime
            );
    }

    private String createAppointment(
        String clinicId,
        String doctorId,
        String patientId,
        String startTime,
        String endTime
    ) throws Exception {

        String response = mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-20",
                    startTime,
                    endTime
                )))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return extractId(response);
    }

    @Test
    void shouldCreateAppointmentSuccessfully() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-20",
                    "10:00:00",
                    "10:30:00"
                )))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.doctorId").value(doctorId))
            .andExpect(jsonPath("$.patientId").value(patientId))
            .andExpect(jsonPath("$.doctorName").value("Dr. Appointment Test"))
            .andExpect(jsonPath("$.patientName").value("Appointment Test Patient"))
            .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldRetrieveAppointmentById() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId,
            doctorId,
            patientId,
            "11:00:00",
            "11:30:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(appointmentId))
            .andExpect(jsonPath("$.clinicId").value(clinicId))
            .andExpect(jsonPath("$.doctorId").value(doctorId))
            .andExpect(jsonPath("$.patientId").value(patientId));
    }

    @Test
    void shouldRetrieveAllAppointmentsForClinic() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        createAppointment(
            clinicId, doctorId, patientId, "09:00:00", "09:30:00"
        );

        createAppointment(
            clinicId, doctorId, patientId, "09:30:00", "10:00:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRejectInvalidAppointmentData() throws Exception {

        String clinicId = createClinic();

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": null,
                        "patientId": null,
                        "appointmentDate": "2027-05-20",
                        "startTime": "10:00:00",
                        "endTime": "10:30:00"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAppointmentWithInvalidTimeRange() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-20",
                    "11:00:00",
                    "10:00:00"
                )))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForMissingClinic() throws Exception {

        String missingClinicId = "00000000-0000-0000-0000-000000000001";

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", missingClinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "doctorId": "00000000-0000-0000-0000-000000000002",
                        "patientId": "00000000-0000-0000-0000-000000000003",
                        "appointmentDate": "2027-05-20",
                        "startTime": "10:00:00",
                        "endTime": "10:30:00"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForMissingAppointment() throws Exception {

        String clinicId = createClinic();
        String missingAppointmentId =
            "00000000-0000-0000-0000-000000000004";

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                missingAppointmentId))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreventCrossClinicDoctorAndPatientAccess() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String doctorId = createDoctor(clinicOne);
        String patientId = createPatient(clinicOne);

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicTwo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-20",
                    "10:00:00",
                    "10:30:00"
                )))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectOverlappingAppointmentsForSameDoctor() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientOne = createPatient(clinicId);
        String patientTwo = createPatient(clinicId);

        createAppointment(
            clinicId, doctorId, patientOne, "10:00:00", "10:30:00"
        );

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientTwo,
                    "2027-05-20",
                    "10:15:00",
                    "10:45:00"
                )))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldConfirmScheduledAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "CONFIRMED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldCancelScheduledAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "11:00:00", "11:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "CANCELLED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldCompleteConfirmedAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "12:00:00", "12:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "CONFIRMED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "COMPLETED"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldRejectInvalidAppointmentStatusTransition() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "13:00:00", "13:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "COMPLETED"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPreventCrossClinicAppointmentStatusUpdate() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String doctorId = createDoctor(clinicOne);
        String patientId = createPatient(clinicOne);

        String appointmentId = createAppointment(
            clinicOne, doctorId, patientId, "14:00:00", "14:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicTwo,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "CONFIRMED"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingAppointmentStatus()
        throws Exception {

        String clinicId = createClinic();

        String missingAppointmentId =
            "00000000-0000-0000-0000-000000000005";

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                missingAppointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "status": "CONFIRMED"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectMissingAppointmentStatus() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "15:00:00", "15:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateAppointmentSuccessfully() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-21",
                    "11:00:00",
                    "11:45:00"
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(appointmentId))
            .andExpect(jsonPath("$.appointmentDate").value("2027-05-21"))
            .andExpect(jsonPath("$.startTime").value("11:00:00"))
            .andExpect(jsonPath("$.endTime").value("11:45:00"))
            .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void shouldAllowUpdatingAppointmentWithoutSelfConflict()
        throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-20",
                    "10:15:00",
                    "10:45:00"
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.startTime").value("10:15:00"));
    }

    @Test
    void shouldRejectOverlappingAppointmentUpdate() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientOne = createPatient(clinicId);
        String patientTwo = createPatient(clinicId);

        String appointmentOne = createAppointment(
            clinicId, doctorId, patientOne, "10:00:00", "10:30:00"
        );

        createAppointment(
            clinicId, doctorId, patientTwo, "11:00:00", "11:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentOne)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientOne,
                    "2027-05-20",
                    "11:15:00",
                    "11:45:00"
                )))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldPreventCrossClinicAppointmentUpdate() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String doctorId = createDoctor(clinicOne);
        String patientId = createPatient(clinicOne);

        String appointmentId = createAppointment(
            clinicOne, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicTwo,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-21",
                    "11:00:00",
                    "11:30:00"
                )))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectDoctorFromAnotherClinicDuringUpdate()
        throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String doctorOne = createDoctor(clinicOne);
        String doctorTwo = createDoctor(clinicTwo);
        String patientId = createPatient(clinicOne);

        String appointmentId = createAppointment(
            clinicOne, doctorOne, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicOne,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorTwo,
                    patientId,
                    "2027-05-21",
                    "11:00:00",
                    "11:30:00"
                )))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectPatientFromAnotherClinicDuringUpdate()
        throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String doctorId = createDoctor(clinicOne);
        String patientOne = createPatient(clinicOne);
        String patientTwo = createPatient(clinicTwo);

        String appointmentId = createAppointment(
            clinicOne, doctorId, patientOne, "10:00:00", "10:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicOne,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientTwo,
                    "2027-05-21",
                    "11:00:00",
                    "11:30:00"
                )))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectAppointmentUpdateWithInvalidTimeRange()
        throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-21",
                    "12:00:00",
                    "11:00:00"
                )))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdatingCompletedAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "CONFIRMED"}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "COMPLETED"}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-21",
                    "11:00:00",
                    "11:30:00"
                )))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUpdatingCancelledAppointment() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "CANCELLED"}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createAppointmentRequest(
                    doctorId,
                    patientId,
                    "2027-05-21",
                    "11:00:00",
                    "11:30:00"
                )))
            .andExpect(status().isBadRequest());
    }
}