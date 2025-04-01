package com.optimed.web;

import com.optimed.client.NotificationClient;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@ContextConfiguration(classes = DoctorControllerTests.TestConfig.class)
public class DoctorControllerTests {

    @Configuration
    @Import(DoctorController.class)
    static class TestConfig {
        @Bean
        public AppointmentService appointmentService() {
            return mock(AppointmentService.class);
        }

        @Bean
        public DoctorService doctorService() {
            return mock(DoctorService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public MedicalRecordService medicalRecordService() {
            return mock(MedicalRecordService.class);
        }

        @Bean
        public PrescriptionService prescriptionService() {
            return mock(PrescriptionService.class);
        }

        @Bean
        public PatientService patientService() {
            return mock(PatientService.class);
        }

        @Bean
        public ReviewService reviewService() {
            return mock(ReviewService.class);
        }

        @Bean
        public NotificationClient notificationClient() {
            return mock(NotificationClient.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private NotificationClient notificationClient;

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getDashboard_shouldReturnDashboardViewWithCorrectAttributes() throws Exception {
        // Given
        User user = new User();
        user.setEmail("doctor@example.com");

        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(doctorService.getDoctorFullName(anyString())).thenReturn("Dr. John Doe");
        when(notificationClient.getNotificationsByEmail(anyString())).thenReturn(CollectionModel.empty());
        when(patientService.getRecentPatients(anyString())).thenReturn(new ArrayList<>());
        when(appointmentService.getCanceledAppointments()).thenReturn (1L);
        when(appointmentService.getTodaysAppointments()).thenReturn (2L);
        when(appointmentService.getBookedAppointments()).thenReturn (2L);
        when(appointmentService.getUpcomingAppointmentsForMonth()).thenReturn(new ArrayList<>());

        // When
        MockHttpServletRequestBuilder request = get("/doctor/dashboard");

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/dashboard"))
                .andExpect(model().attributeExists("doctor", "recentPatients", "notifications",
                        "cancelledAppointments", "todaysAppointments", "bookedAppointments",
                        "upcomingAppointments", "currentUserPage"));

        verify(userService, times(1)).findByUsername(anyString());
        verify(doctorService, times(1)).getDoctorFullName(anyString());
        verify(notificationClient, times(1)).registerDoctorIfNotExists(anyString());
        verify(notificationClient, times(1)).getNotificationsByEmail(anyString());
        verify(patientService, times(1)).getRecentPatients(anyString());
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getDoctorAppointments_shouldReturnAppointmentsView() throws Exception {
        // Given
        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(UUID.randomUUID());

        List<Appointment> appointments = new ArrayList<>();
        PageImpl<Appointment> appointmentPage = new PageImpl<>(appointments);

        when(doctorService.findByUsername(anyString())).thenReturn(Optional.of(doctor));
        when(appointmentService.searchAppointments(
                any(UUID.class),
                any(AppointmentStatus.class),
                anyString(),
                any(LocalDate.class),
                any(LocalDate.class),
                any(Pageable.class)))
                .thenReturn(appointmentPage);

        // When
        MockHttpServletRequestBuilder request = get("/doctor/appointments")
                .param("page", "0")
                .param("size", "10");

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/appointments/list-appointments"))
                .andExpect(model().attributeExists("appointments", "totalPages", "totalItems",
                        "currentUserPage", "currentPage", "pageSize"));

        verify(doctorService, times(1)).findByUsername(anyString());
        verify(appointmentService, times(1)).searchAppointments(
                any(UUID.class),
                any(),
                anyString(),
                any(),
                any(),
                any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void approveAppointment_shouldRedirectToAppointments() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        doNothing().when(appointmentService).approveAppointment(any(UUID.class));

        // When
        MockHttpServletRequestBuilder request = post("/doctor/appointments/" + appointmentId + "/approve")
                .with(csrf());

        // Then
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"))
                .andExpect(flash().attributeExists("success"));

        verify(appointmentService, times(1)).approveAppointment(appointmentId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void approveAppointment_whenExceptionThrown_shouldRedirectWithErrorMessage() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        doThrow(new RuntimeException("Test error")).when(appointmentService).approveAppointment(any(UUID.class));

        // When
        MockHttpServletRequestBuilder request = post("/doctor/appointments/" + appointmentId + "/approve")
                .with(csrf());

        // Then
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"))
                .andExpect(flash().attributeExists("error"));

        verify(appointmentService, times(1)).approveAppointment(appointmentId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void rejectAppointment_shouldRedirectToAppointments() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        doNothing().when(appointmentService).updateAppointmentStatus(any(UUID.class), any(AppointmentStatus.class));

        // When
        MockHttpServletRequestBuilder request = post("/doctor/appointments/" + appointmentId + "/reject")
                .with(csrf());

        // Then
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"));

        verify(appointmentService, times(1)).updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELED);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void showRescheduleForm_shouldReturnRescheduleView() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        when(appointmentService.getAppointmentById(any(UUID.class))).thenReturn(appointment);

        // When
        MockHttpServletRequestBuilder request = get("/doctor/appointments/" + appointmentId + "/reschedule");

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/appointments/reschedule-appointment"))
                .andExpect(model().attributeExists("appointment", "currentUserPage"));

        verify(appointmentService, times(1)).getAppointmentById(appointmentId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void rescheduleAppointment_shouldRedirectToAppointments() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        LocalDate newDate = LocalDate.now().plusDays(7);
        LocalTime newTime = LocalTime.of(10, 30);

        doNothing().when(appointmentService).rescheduleAppointment(any(UUID.class), any(LocalDate.class), any(LocalTime.class));

        // When
        MockHttpServletRequestBuilder request = post("/doctor/appointments/" + appointmentId + "/reschedule")
                .param("newDate", newDate.toString())
                .param("newTime", newTime.toString())
                .with(csrf());

        // Then
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"));

        verify(appointmentService, times(1)).rescheduleAppointment(appointmentId, newDate, newTime);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getAppointmentDetails_shouldReturnDetailsView() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        when(appointmentService.getAppointmentById(any(UUID.class))).thenReturn(appointment);

        // When
        MockHttpServletRequestBuilder request = get("/doctor/appointments/" + appointmentId);

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/appointments/appointment-details"))
                .andExpect(model().attributeExists("appointment", "currentUserPage"));

        verify(appointmentService, times(1)).getAppointmentById(appointmentId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getAppointmentDetails_whenNotFound_shouldReturnErrorView() throws Exception {
        // Given
        UUID appointmentId = UUID.randomUUID();

        when(appointmentService.getAppointmentById(any(UUID.class))).thenThrow(new NoSuchElementException("Not found"));

        // When
        MockHttpServletRequestBuilder request = get("/doctor/appointments/" + appointmentId);

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attributeExists("error", "currentUserPage"));

        verify(appointmentService, times(1)).getAppointmentById(appointmentId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getDoctorSettings_shouldReturnSettingsView() throws Exception {
        // Given
        DoctorProfile doctorProfile = new DoctorProfile();
        doctorProfile.setId(UUID.randomUUID());

        when(doctorService.findByUsername(anyString())).thenReturn(Optional.of(doctorProfile));

        // When
        MockHttpServletRequestBuilder request = get("/doctor/settings");

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/settings"))
                .andExpect(model().attributeExists("doctor", "specializations", "currentUserPage"));

        verify(doctorService, times(1)).findByUsername(anyString());
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void viewPatientDetails_shouldReturnPatientDetailsView() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();
        PatientProfile patient = new PatientProfile();
        patient.setId(patientId);

        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(UUID.randomUUID());

        List<Prescription> prescriptions = new ArrayList<>();
        List<MedicalRecord> records = new ArrayList<>();

        when(patientService.getPatientById(any(UUID.class))).thenReturn(patient);
        when(doctorService.findByUsername(anyString())).thenReturn(Optional.of(doctor));
        when(prescriptionService.getPrescriptionsForPatient(any(UUID.class))).thenReturn(prescriptions);
        when(medicalRecordService.getRecordsForPatient(any(UUID.class))).thenReturn(records);

        // When
        MockHttpServletRequestBuilder request = get("/doctor/patient/" + patientId);

        // Then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/patients/patient-details"))
                .andExpect(model().attributeExists("patient", "doctor", "prescriptions", "medicalRecords", "currentUserPage"));

        verify(patientService, times(1)).getPatientById(patientId);
        verify(doctorService, times(1)).findByUsername(anyString());
        verify(prescriptionService, times(1)).getPrescriptionsForPatient(patientId);
        verify(medicalRecordService, times(1)).getRecordsForPatient(patientId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getDoctorPatients_shouldReturnPatientListViewWithPatients() throws Exception {
        // Given
        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(UUID.randomUUID());
        List<PatientProfile> patients = List.of(new PatientProfile());

        when(doctorService.findByUsername(anyString())).thenReturn(Optional.of(doctor));
        when(appointmentService.getPatientsByDoctor(any(UUID.class))).thenReturn(patients);

        // When/Then
        mockMvc.perform(get("/doctor/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/patients/patient-list"))
                .andExpect(model().attributeExists("patients", "currentUserPage"));

        verify(doctorService).findByUsername(anyString());
        verify(appointmentService).getPatientsByDoctor(doctor.getId());
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void viewDoctorReviews_shouldReturnReviewsViewWithReviews() throws Exception {
        // Given
        DoctorProfile doctor = new DoctorProfile();
        doctor.setId(UUID.randomUUID());
        List<Review> reviews = List.of(new Review());

        when(doctorService.findByUsername(anyString())).thenReturn(Optional.of(doctor));
        when(reviewService.getDoctorReviews(any(UUID.class))).thenReturn(reviews);

        // When/Then
        mockMvc.perform(get("/doctor/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/reviews"))
                .andExpect(model().attributeExists("reviews"));

        verify(doctorService).findByUsername(anyString());
        verify(reviewService).getDoctorReviews(doctor.getId());
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void deleteReview_shouldRedirectToReviewsWithSuccessMessage() throws Exception {
        // Given
        UUID reviewId = UUID.randomUUID();
        doNothing().when(reviewService).deleteReview(any(UUID.class));

        // When/Then
        mockMvc.perform(post("/doctor/reviews/delete/" + reviewId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/reviews"))
                .andExpect(flash().attributeExists("success"));

        verify(reviewService).deleteReview(reviewId);
    }

    @Test
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void deleteReview_whenExceptionThrown_shouldRedirectWithErrorMessage() throws Exception {
        // Given
        UUID reviewId = UUID.randomUUID();
        doThrow(new RuntimeException("Error")).when(reviewService).deleteReview(any(UUID.class));

        // When/Then
        mockMvc.perform(post("/doctor/reviews/delete/" + reviewId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/reviews"))
                .andExpect(flash().attributeExists("error"));

        verify(reviewService).deleteReview(reviewId);
    }
}