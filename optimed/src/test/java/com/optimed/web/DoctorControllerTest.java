package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.client.NotificationClient;
import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.enums.Specialization;
import com.optimed.service.*;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.*;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;
    @MockitoBean
    private DoctorService doctorService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private MedicalRecordService medicalRecordService;
    @MockitoBean
    private PrescriptionService prescriptionService;
    @MockitoBean
    private PatientService patientService;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private NotificationClient notificationClient;

    private DoctorProfile doctorProfile;
    private PatientProfile patientProfile;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctorProfile = DoctorProfile.builder()
                .fullName ("doctor1")
                .user(User.builder().email("doctor1@example.com").build())
                .fullName("Dr. Test")
                .specialization(Specialization.CARDIOLOGY)
                .build();

        patientProfile = PatientProfile.builder()
                .id(UUID.randomUUID())
                .fullName ("Test Patient")
                .build();

        appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .status(AppointmentStatus.BOOKED)
                .patient(patientProfile)
                .doctor(doctorProfile)
                .appointmentDate(LocalDate.now().plusDays(2))
                .build();
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldReturnDoctorDashboard() throws Exception {
        mockMvc.perform(get("/doctor/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/dashboard"))
                .andExpect(model().attribute("doctor", "Dr. Test"))
                .andExpect(model().attributeExists("recentPatients"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("upcomingAppointments"));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldReturnDoctorAppointmentsPage() throws Exception {
        DoctorProfile doctorProfile = DoctorProfile.builder()
                .id(UUID.randomUUID())
                .fullName("Dr. Test")
                .specialization(Specialization.CARDIOLOGY)
                .build();

        when(doctorService.findByUsername("doctor1")).thenReturn(Optional.of(doctorProfile));

        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctor(doctorProfile)
                .status(AppointmentStatus.BOOKED)
                .patient(PatientProfile.builder().fullName("Test Patient").build())
                .appointmentDate(LocalDate.now().plusDays(2))
                .build();

        List<Appointment> appointments = List.of(appointment);

        mockMvc.perform(get("/doctor/appointments"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/appointments/list-appointments"))
                .andExpect(model().attributeExists("appointments"))
                .andExpect(model().attribute("appointments", appointments));
    }


    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldApproveAppointment() throws Exception {
        UUID appointmentId = appointment.getId();
        doNothing().when(appointmentService).approveAppointment(appointmentId);

        mockMvc.perform(post("/doctor/appointments/{appointmentId}/approve", appointmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"))
                .andExpect(flash().attribute("success", "Appointment approved successfully."));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldRejectAppointment() throws Exception {
        UUID appointmentId = appointment.getId();

        mockMvc.perform(post("/doctor/appointments/{appointmentId}/reject", appointmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldCancelAppointment() throws Exception {
        UUID appointmentId = appointment.getId();

        mockMvc.perform(post("/doctor/appointments/{appointmentId}/cancel", appointmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/appointments"));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldReturnAppointmentDetailsPage() throws Exception {
        when(appointmentService.getAppointmentById(appointment.getId())).thenReturn(appointment);

        mockMvc.perform(get("/doctor/appointments/{appointmentId}", appointment.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/appointments/appointment-details"))
                .andExpect(model().attribute("appointment", appointment))
                .andExpect(model().attribute("appointment", hasProperty("patient", notNullValue())));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldReturnErrorWhenAppointmentNotFound() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.getAppointmentById(appointmentId)).thenThrow(new NoSuchElementException ());

        mockMvc.perform(get("/doctor/appointments/{appointmentId}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("error", "Appointment not found."));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldReturnDoctorSettingsPage() throws Exception {
        EditDoctorRequest editDoctorRequest = EditDoctorRequest.builder()
                .fullName("Dr. Test")
                .specialization(Specialization.CARDIOLOGY)
                .avatarUrl("/dashboard/img/default.png")
                .build();

        when(doctorService.findByUsername("doctor1")).thenReturn(Optional.of(doctorProfile));

        mockMvc.perform(get("/doctor/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/settings"))
                .andExpect(model().attribute("doctor", editDoctorRequest))
                .andExpect(model().attributeExists("specializations"));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldUpdateDoctorProfile() throws Exception {
        EditDoctorRequest editDoctorRequest = EditDoctorRequest.builder()
                .fullName("Dr. Updated")
                .specialization(Specialization.CARDIOLOGY)
                .avatarUrl("/dashboard/img/default.png")
                .build();

        mockMvc.perform(post("/doctor/settings")
                        .flashAttr("editDoctorRequest", editDoctorRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/dashboard"));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldViewPatientDetails() throws Exception {
        UUID patientId = patientProfile.getId();
        when(patientService.getPatientById(patientId)).thenReturn(patientProfile);

        mockMvc.perform(get("/doctor/patient/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/patients/patient-details"))
                .andExpect(model().attribute("patient", patientProfile));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldViewDoctorReviews() throws Exception {
        List<Review> reviews = List.of(new Review());
        when(reviewService.getDoctorReviews(doctorProfile.getId())).thenReturn(reviews);

        mockMvc.perform(get("/doctor/reviews"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/reviews"))
                .andExpect(model().attribute("reviews", reviews));
    }

    @Test
    @WithMockUser(username = "doctor1", roles = {"DOCTOR"})
    void shouldDeleteReview() throws Exception {
        UUID reviewId = UUID.randomUUID();
        doNothing().when(reviewService).deleteReview(reviewId);

        mockMvc.perform(post("/doctor/reviews/delete/{reviewId}", reviewId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/reviews"))
                .andExpect(flash().attribute("success", "Review deleted successfully."));
    }
}
