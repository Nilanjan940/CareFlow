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
@org.springframework.test.context.ActiveProfiles("local")
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
    void shouldRecordAppointmentCreationInHistory() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String response = mockMvc.perform(post(
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        String appointmentId = extractId(response);

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].action").value("CREATED"))
            .andExpect(jsonPath("$[0].description")
                .value("Appointment created."))
            .andExpect(jsonPath("$[0].oldValues").doesNotExist())
            .andExpect(jsonPath("$[0].newValues.doctorId").value(doctorId))
            .andExpect(jsonPath("$[0].newValues.patientId").value(patientId))
            .andExpect(jsonPath("$[0].newValues.appointmentDate")
                .value("2027-05-20"))
            .andExpect(jsonPath("$[0].newValues.startTime").value("10:00"))
            .andExpect(jsonPath("$[0].newValues.endTime").value("10:30"))
            .andExpect(jsonPath("$[0].newValues.status").value("SCHEDULED"));
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
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2));
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
    void shouldRecordAppointmentStatusChangeInHistory() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId,
            doctorId,
            patientId,
            "10:00:00",
            "10:30:00"
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

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].action").value("STATUS_CHANGED"))
            .andExpect(jsonPath("$[0].description")
                .value("Appointment status changed from SCHEDULED to CONFIRMED."))
            .andExpect(jsonPath("$[0].oldValues.status").value("SCHEDULED"))
            .andExpect(jsonPath("$[0].newValues.status").value("CONFIRMED"))
            .andExpect(jsonPath("$[1].action").value("CREATED"));
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
    void shouldCancelAppointmentWithReasonAndRecordHistory()
        throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "11:00:00", "11:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/cancel",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "reason": "Patient requested cancellation"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.cancellationReason")
                .value("Patient requested cancellation"));

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].action").value("CANCELLED"))
            .andExpect(jsonPath("$[0].newValues.status").value("CANCELLED"))
            .andExpect(jsonPath("$[0].newValues.cancellationReason")
                .value("Patient requested cancellation"))
            .andExpect(jsonPath("$[0].oldValues.status").value("SCHEDULED"))
            .andExpect(jsonPath("$[1].action").value("CREATED"));
    }
    @Test
    void shouldCancelAppointmentWithoutReason() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "14:00:00", "14:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/cancel",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"))
            .andExpect(jsonPath("$.cancellationReason").doesNotExist());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].action").value("CANCELLED"))
            .andExpect(jsonPath("$[0].newValues.status").value("CANCELLED"))
            .andExpect(jsonPath("$[0].newValues.cancellationReason").doesNotExist());
    }
    @Test
    void shouldRejectCancellationReasonLongerThan500Characters()
        throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "11:00:00", "11:30:00"
        );

        String longReason = "A".repeat(501);

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/cancel",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason": "%s"}
                    """.formatted(longReason)))
            .andExpect(status().isBadRequest());
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
    void shouldRejectCancellationOfCompletedAppointment()
        throws Exception {

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

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/cancel",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason": "Late cancellation attempt"}
                    """))
            .andExpect(status().isBadRequest());
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
    void shouldRecordAppointmentUpdateInHistory() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId,
            doctorId,
            patientId,
            "10:00:00",
            "10:30:00"
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
            .andExpect(status().isOk());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].action").value("RESCHEDULED"))
            .andExpect(jsonPath("$[0].oldValues.appointmentDate")
                .value("2027-05-20"))
            .andExpect(jsonPath("$[0].oldValues.startTime").value("10:00"))
            .andExpect(jsonPath("$[0].oldValues.endTime").value("10:30"))
            .andExpect(jsonPath("$[0].newValues.appointmentDate")
                .value("2027-05-21"))
            .andExpect(jsonPath("$[0].newValues.startTime").value("11:00"))
            .andExpect(jsonPath("$[0].newValues.endTime").value("11:45"))
            .andExpect(jsonPath("$[0].newValues.status").value("SCHEDULED"))
            .andExpect(jsonPath("$[1].action").value("CREATED"));
    }
    @Test
    void shouldRecordOrdinaryAppointmentUpdateInHistory()
        throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        String updateRequest = createAppointmentRequest(
            doctorId,
            patientId,
            "2027-05-20",
            "10:00:00",
            "10:30:00"
        )
            .replace("Regular consultation", "Follow-up consultation")
            .replace(
                "Integration test appointment",
                "Updated consultation notes"
            );

        mockMvc.perform(put(
                "/api/clinics/{clinicId}/appointments/{appointmentId}",
                clinicId,
                appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isOk());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                appointmentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].action").value("UPDATED"))
            .andExpect(jsonPath("$[0].oldValues.reason")
                .value("Regular consultation"))
            .andExpect(jsonPath("$[0].newValues.reason")
                .value("Follow-up consultation"))
            .andExpect(jsonPath("$[0].oldValues.notes")
                .value("Integration test appointment"))
            .andExpect(jsonPath("$[0].newValues.notes")
                .value("Updated consultation notes"))
            .andExpect(jsonPath("$[1].action").value("CREATED"));
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
    void shouldReturnNotFoundForMissingAppointmentHistory() throws Exception {

        String clinicId = createClinic();

        String missingAppointmentId =
            "00000000-0000-0000-0000-000000000001";

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                clinicId,
                missingAppointmentId))
            .andExpect(status().isNotFound());
    }
    @Test
    void shouldNotRetrieveAppointmentHistoryFromAnotherClinic()
        throws Exception {

        String clinicId = createClinic();
        String anotherClinicId = createClinic();

        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId,
            doctorId,
            patientId,
            "10:00:00",
            "10:30:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/history",
                anotherClinicId,
                appointmentId))
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

    
    @Test
    void shouldFilterAppointmentsByDate() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String request = createAppointmentRequest(
            doctorId, patientId, "2027-05-21", "10:00:00", "10:30:00"
        );

        mockMvc.perform(post(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("date", "2027-05-21"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].appointmentDate").value("2027-05-21"));
    }

    @Test
    void shouldFilterAppointmentsByStatus() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId, appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "CONFIRMED"}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("status", "CONFIRMED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

    @Test
    void shouldFilterAppointmentsByDoctor() throws Exception {

        String clinicId = createClinic();
        String doctorOne = createDoctor(clinicId);
        String doctorTwo = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        createAppointment(
            clinicId, doctorOne, patientId, "10:00:00", "10:30:00"
        );

        createAppointment(
            clinicId, doctorTwo, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("doctorId", doctorOne))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].doctorId").value(doctorOne));
    }

    @Test
    void shouldFilterAppointmentsByPatient() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientOne = createPatient(clinicId);
        String patientTwo = createPatient(clinicId);

        createAppointment(
            clinicId, doctorId, patientOne, "10:00:00", "10:30:00"
        );

        createAppointment(
            clinicId, doctorId, patientTwo, "10:30:00", "11:00:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("patientId", patientOne))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].patientId").value(patientOne));
    }

    @Test
    void shouldCombineMultipleAppointmentFilters() throws Exception {

        String clinicId = createClinic();
        String doctorOne = createDoctor(clinicId);
        String doctorTwo = createDoctor(clinicId);
        String patientOne = createPatient(clinicId);
        String patientTwo = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId, doctorOne, patientOne, "10:00:00", "10:30:00"
        );

        createAppointment(
            clinicId, doctorTwo, patientTwo, "10:30:00", "11:00:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId, appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "CONFIRMED"}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("date", "2027-05-20")
                .param("status", "CONFIRMED")
                .param("doctorId", doctorOne)
                .param("patientId", patientOne))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(appointmentId));
    }

    @Test
    void shouldReturnEmptyListWhenNoAppointmentsMatchFilters()
        throws Exception {

        String clinicId = createClinic();

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("date", "2027-05-21"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldPreventCrossClinicAppointmentSearch() throws Exception {

        String clinicOne = createClinic();
        String clinicTwo = createClinic();

        String doctorId = createDoctor(clinicOne);
        String patientId = createPatient(clinicOne);

        createAppointment(
            clinicOne, doctorId, patientId, "10:00:00", "10:30:00"
        );

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicTwo)
                .param("date", "2027-05-20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void shouldReturnDefaultAppointmentPageWithMetadata() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String[] startTimes = {
            "08:00:00", "08:30:00",
            "09:00:00", "09:30:00",
            "10:00:00", "10:30:00",
            "11:00:00", "11:30:00",
            "12:00:00", "12:30:00",
            "13:00:00", "13:30:00"
        };

        String[] endTimes = {
            "08:30:00", "09:00:00",
            "09:30:00", "10:00:00",
            "10:30:00", "11:00:00",
            "11:30:00", "12:00:00",
            "12:30:00", "13:00:00",
            "13:30:00", "14:00:00"
        };

        for (int i = 0; i < startTimes.length; i++) {
            createAppointment(
                clinicId,
                doctorId,
                patientId,
                startTimes[i],
                endTimes[i]
            );
        }

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(12))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(false))
            .andExpect(jsonPath("$.content.length()").value(10));
    }

    @Test
    void shouldReturnRequestedAppointmentPage() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String[] startTimes = {
            "08:00:00", "08:30:00",
            "09:00:00", "09:30:00",
            "10:00:00", "10:30:00",
            "11:00:00", "11:30:00",
            "12:00:00", "12:30:00",
            "13:00:00", "13:30:00"
        };

        String[] endTimes = {
            "08:30:00", "09:00:00",
            "09:30:00", "10:00:00",
            "10:30:00", "11:00:00",
            "11:30:00", "12:00:00",
            "12:30:00", "13:00:00",
            "13:30:00", "14:00:00"
        };

        for (int i = 0; i < startTimes.length; i++) {
            createAppointment(
                clinicId,
                doctorId,
                patientId,
                startTimes[i],
                endTimes[i]
            );
        }

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("page", "1")
                .param("size", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(5))
            .andExpect(jsonPath("$.totalElements").value(12))
            .andExpect(jsonPath("$.totalPages").value(3))
            .andExpect(jsonPath("$.first").value(false))
            .andExpect(jsonPath("$.last").value(false))
            .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    void shouldSortAppointmentsByDateAndStartTime() throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String[] dates = {
            "2027-05-21",
            "2027-05-20",
            "2027-05-20"
        };

        String[] startTimes = {
            "10:00:00",
            "11:00:00",
            "09:00:00"
        };

        String[] endTimes = {
            "10:30:00",
            "11:30:00",
            "09:30:00"
        };

        for (int i = 0; i < dates.length; i++) {
            mockMvc.perform(post(
                    "/api/clinics/{clinicId}/appointments", clinicId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createAppointmentRequest(
                        doctorId,
                        patientId,
                        dates[i],
                        startTimes[i],
                        endTimes[i]
                    )))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("sort", "appointmentDate,asc")
                .param("sort", "startTime,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(3))
            .andExpect(jsonPath("$.content[0].appointmentDate")
                .value("2027-05-20"))
            .andExpect(jsonPath("$.content[0].startTime")
                .value("09:00:00"))
            .andExpect(jsonPath("$.content[1].startTime")
                .value("11:00:00"))
            .andExpect(jsonPath("$.content[2].appointmentDate")
                .value("2027-05-21"));
    }

    @Test
    void shouldPreserveFiltersWhenUsingAppointmentPagination()
        throws Exception {

        String clinicId = createClinic();
        String doctorId = createDoctor(clinicId);
        String patientId = createPatient(clinicId);

        String appointmentId = createAppointment(
            clinicId,
            doctorId,
            patientId,
            "10:00:00",
            "10:30:00"
        );

        createAppointment(
            clinicId,
            doctorId,
            patientId,
            "10:30:00",
            "11:00:00"
        );

        mockMvc.perform(patch(
                "/api/clinics/{clinicId}/appointments/{appointmentId}/status",
                clinicId, appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "CONFIRMED"}
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(get(
                "/api/clinics/{clinicId}/appointments", clinicId)
                .param("status", "CONFIRMED")
                .param("page", "0")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(appointmentId))
            .andExpect(jsonPath("$.content[0].status").value("CONFIRMED"));
    }

}