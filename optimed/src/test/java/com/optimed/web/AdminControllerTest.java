package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.dto.AppointmentRequest;
import com.optimed.dto.UserRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.entity.enums.Role;
import com.optimed.entity.enums.Specialization;
import com.optimed.service.*;
import com.optimed.client.NotificationClient;
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
import org.springframework.data.domain.*;

import java.time.*;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private NotificationClient notificationClient;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private DashboardService dashboardService;

    private UUID userId;
    private UUID appointmentId;
    private User user;
    private UUID doctorId;
    private UUID patientId;
    private DoctorProfile doctor;
    private PatientProfile patient;
    private List<TimeSlot> timeSlots;
    private AppointmentRequest appointmentRequest;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .enabled(true)
                .role(Role.PATIENT)
                .build();

        doctor = DoctorProfile.builder()
                .id(doctorId)
                .user(user)
                .email("test@example.com")
                .fullName("Dr. Smith")
                .specialization(Specialization.CARDIOLOGY)
                .availableTimeSlots(timeSlots)
                .build();

        patient = PatientProfile.builder()
                .id(patientId)
                .user(user)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .build();

        appointment = Appointment.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .build();

        timeSlots = List.of(
                TimeSlot.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnDashboard() throws Exception {
        when(userService.countUsers()).thenReturn(100L);
        when(doctorService.countDoctors()).thenReturn(50L);
        when(patientService.countPatients()).thenReturn(150L);
        when(appointmentService.countAppointments()).thenReturn(200L);
        when(userService.getRecentUsers()).thenReturn(List.of(new User()));
        when(appointmentService.getRecentAppointments()).thenReturn(List.of(new Appointment()));

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalUsers", 100L))
                .andExpect(model().attribute("totalDoctors", 50L))
                .andExpect(model().attribute("totalPatients", 150L))
                .andExpect(model().attribute("totalAppointments", 200L))
                .andExpect(model().attribute("recentUsers", hasSize(1)))
                .andExpect(model().attribute("recentAppointments", hasSize(1)))
                .andExpect(model().attribute("currentPage", "Dashboard"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnDashboardStats() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        when(userService.countUsers()).thenReturn(10L);
        when(doctorService.countDoctors()).thenReturn(5L);
        when(patientService.countPatients()).thenReturn(15L);
        when(appointmentService.countAppointments()).thenReturn(30L);

        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFetchUsers() throws Exception {
        Page<User> users = new PageImpl<>(List.of(new User()));
        when(userService.getAllNonAdminUsers(any(Pageable.class))).thenReturn(users);

        mockMvc.perform(get("/admin/manage-users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(post("/admin/delete-user/{userId}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-users"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldEditUser() throws Exception {
        User user = new User();
        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/admin/edit-user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("newUser");
        userRequest.setEmail("newUser@example.com");
        userRequest.setRole(Role.PATIENT);

        doNothing().when(userService).updateUser(eq(userId), any(UserRequest.class));

        mockMvc.perform(post("/admin/edit-user/{userId}", userId)
                        .param("id", userId.toString())
                        .param("username", userRequest.getUsername())
                        .param("email", userRequest.getEmail())
                        .param("role", userRequest.getRole().name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-users?updated=true"));

        verify(userService, times(1)).updateUser(eq(userId), any(UserRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFetchAppointments() throws Exception {

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        Page<Appointment> appointments = new PageImpl<>(List.of(appointment));
        when(appointmentService.getAllAppointments(any(Pageable.class))).thenReturn(appointments);

        mockMvc.perform(get("/admin/manage-appointments"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("appointments"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteAppointment() throws Exception {
        doNothing().when(appointmentService).deleteAppointment(appointmentId);

        mockMvc.perform(post("/admin/delete-appointment/{appointmentId}", appointmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-appointments"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldRescheduleAppointment() throws Exception {
        doNothing().when(appointmentService).rescheduleAppointment(any(), any(), any());

        mockMvc.perform(post("/admin/appointments/{appointmentId}/reschedule", appointmentId)
                        .param("newDate", "2025-04-10")
                        .param("newTime", "14:00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-appointments"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCancelAppointment() throws Exception {
        doNothing().when(appointmentService).cancelAppointment(appointmentId);

        mockMvc.perform(post("/admin/appointments/{appointmentId}/cancel", appointmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-appointments"));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetSettingsPage() throws Exception {
        User user = new User();
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/admin/settings"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userRequest"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldShowRescheduleAppointmentForm() throws Exception {

        when(appointmentService.getAppointmentById(any(UUID.class))).thenReturn(appointment);

        mockMvc.perform(get("/admin/appointments/{appointmentId}/reschedule", appointment.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/manage-appointments"))
                .andExpect(model().attribute("currentUserPage", "Reschedule Appointment"))
                .andExpect(model().attribute("appointment", appointment));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateAdminSettings() throws Exception {
        User user = new User();
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(post("/admin/settings")
                        .param("username", "adminUser")
                        .param("email", "admin@example.com"))
                .andExpect(status().is2xxSuccessful ());
    }
}
