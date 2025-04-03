package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.client.NotificationClient;
import com.optimed.dto.AppointmentRequest;
import com.optimed.dto.EditDoctorRequest;
import com.optimed.dto.EditPatientRequest;
import com.optimed.dto.NotificationRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.entity.enums.Specialization;
import com.optimed.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;
    @MockitoBean
    private DoctorProfileService doctorProfileService;
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

    @MockitoBean
    private DoctorService doctorService;

    private PatientProfile patientProfile;
    private DoctorProfile doctorProfile;
    private Appointment appointment;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("patient1")
                .email("patient1@example.com")
                .build();

        patientProfile = PatientProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .fullName("Test Patient")
                .build();

        doctorProfile = DoctorProfile.builder()
                .id(UUID.randomUUID())
                .user(User.builder().email("doctor1@example.com").build())
                .fullName("Dr. Test")
                .specialization(Specialization.CARDIOLOGY)
                .build();

        appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .status(AppointmentStatus.BOOKED)
                .patient(patientProfile)
                .doctor(doctorProfile)
                .appointmentDate(LocalDate.now().plusDays(2))
                .appointmentTime(LocalTime.of(10, 0))
                .build();
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldShowAppointmentForm() throws Exception {
        mockMvc.perform(get("/patient/appointments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/appointments/create"))
                .andExpect(model().attributeExists("appointmentRequest"))
                .andExpect(model().attributeExists("specializations"));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldAddAppointment() throws Exception {
        UUID doctorId = UUID.randomUUID();
        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        LocalTime appointmentTime = LocalTime.of(10, 0);

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentDate(appointmentDate);
        request.setAppointmentTime(appointmentTime);
        request.setSpecialization(Specialization.CARDIOLOGY);

        when(patientService.getPatientByUsername("patient1")).thenReturn(Optional.of(patientProfile));
        when(appointmentService.isDoctorAvailable(doctorId, appointmentDate, appointmentTime)).thenReturn(true);
        when(doctorProfileService.doesDoctorHaveTimeSlot(doctorId, appointmentTime)).thenReturn(true);
        doNothing().when(appointmentService).createAppointment(any(AppointmentRequest.class));

        mockMvc.perform(post("/patient/appointments")
                        .flashAttr("appointmentRequest", request))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"))
                .andExpect(flash().attribute("success", "Appointment created successfully."));

        verify(appointmentService).createAppointment(any(AppointmentRequest.class));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldHandleDoctorNotAvailable() throws Exception {
        UUID doctorId = UUID.randomUUID();
        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        LocalTime appointmentTime = LocalTime.of(10, 0);

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentDate(appointmentDate);
        request.setAppointmentTime(appointmentTime);
        request.setSpecialization(Specialization.CARDIOLOGY);

        when(patientService.getPatientByUsername("patient1")).thenReturn(Optional.of(patientProfile));
        when(appointmentService.isDoctorAvailable(doctorId, appointmentDate, appointmentTime)).thenReturn(false);

        List<DoctorProfile> doctors = Collections.singletonList(doctorProfile);
        when(doctorProfileService.findDoctorsBySpecialization(Specialization.CARDIOLOGY)).thenReturn(doctors);

        mockMvc.perform(post("/patient/appointments")
                        .flashAttr("appointmentRequest", request))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/appointments/create"))
                .andExpect(model().attribute("error", "Doctor is not available at the selected time."))
                .andExpect(model().attribute("doctors", doctors));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldHandleTimeSlotNotExists() throws Exception {
        UUID doctorId = UUID.randomUUID();
        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        LocalTime appointmentTime = LocalTime.of(10, 0);

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentDate(appointmentDate);
        request.setAppointmentTime(appointmentTime);
        request.setSpecialization(Specialization.CARDIOLOGY);

        when(patientService.getPatientByUsername("patient1")).thenReturn(Optional.of(patientProfile));
        when(appointmentService.isDoctorAvailable(doctorId, appointmentDate, appointmentTime)).thenReturn(true);
        when(doctorProfileService.doesDoctorHaveTimeSlot(doctorId, appointmentTime)).thenReturn(false);

        mockMvc.perform(post("/patient/appointments")
                        .flashAttr("appointmentRequest", request))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/appointments/create"))
                .andExpect(model().attribute("error", "The selected time slot does not exist for this doctor."));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldShowDashboard() throws Exception {
        when(userService.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(patientService.findByUser(user)).thenReturn(Optional.of(patientProfile));

        List<NotificationRequest> notificationList = new ArrayList<>();
        CollectionModel<NotificationRequest> notifications = CollectionModel.of(notificationList);
        when(notificationClient.getNotificationsByEmail(user.getEmail())).thenReturn(notifications);

        List<Prescription> prescriptions = new ArrayList<>();
        when(prescriptionService.getPrescriptionsForPatient(patientProfile.getId())).thenReturn(prescriptions);

        List<MedicalRecord> records = new ArrayList<>();
        when(medicalRecordService.getRecordsForPatient(patientProfile.getId())).thenReturn(records);

        when(appointmentService.getNextAppointment(patientProfile.getId())).thenReturn(appointment);

        List<Appointment> recentAppointments = new ArrayList<>();
        when(appointmentService.getRecentAppointments(patientProfile.getId())).thenReturn(recentAppointments);

        mockMvc.perform(get("/patient/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/dashboard"))
                .andExpect(model().attributeExists("patientId"))
                .andExpect(model().attributeExists("prescriptions"))
                .andExpect(model().attributeExists("medicalRecords"))
                .andExpect(model().attributeExists("notifications"))
                .andExpect(model().attributeExists("nextAppointment"))
                .andExpect(model().attributeExists("recentAppointments"))
                .andExpect(model().attributeExists("patient"));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldGetDoctorsBySpecialization() throws Exception {
        List<DoctorProfile> doctors = Collections.singletonList(doctorProfile);
        when(doctorProfileService.findDoctorsBySpecialization(Specialization.CARDIOLOGY)).thenReturn(doctors);

        mockMvc.perform(get("/patient/doctors/{specialization}", Specialization.CARDIOLOGY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName", is("Dr. Test")));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldGetPatientAppointments() throws Exception {
        when(userService.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(patientService.findByUser(user)).thenReturn(Optional.of(patientProfile));

        List<Appointment> appointmentList = Collections.singletonList(appointment);
        Page<Appointment> appointmentsPage = new PageImpl<>(appointmentList);

        when(appointmentService.getAppointmentsByPatientId(
                eq(patientProfile.getId()),
                any(Pageable.class),
                eq(null),
                eq(null),
                eq(null),
                eq(null)
        )).thenReturn(appointmentsPage);

        mockMvc.perform(get("/patient/appointments"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/appointments/list-appointments"))
                .andExpect(model().attributeExists("appointments"))
                .andExpect(model().attributeExists("patientId"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldCancelAppointment() throws Exception {
        UUID appointmentId = appointment.getId();
        doNothing().when(appointmentService).cancelAppointment(appointmentId);

        mockMvc.perform(post("/patient/cancel-appointment/{appointmentId}", appointmentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"))
                .andExpect(flash().attribute("success", "Appointment canceled successfully."));

        verify(appointmentService).cancelAppointment(appointmentId);
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldShowRescheduleForm() throws Exception {
        UUID appointmentId = appointment.getId();
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(appointment);

        mockMvc.perform(get("/patient/appointments/{appointmentId}/reschedule", appointmentId))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/appointments/reschedule-appointment"))
                .andExpect(model().attribute("appointment", appointment));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldGetPatientSettings() throws Exception {
        EditPatientRequest editPatientRequest = EditPatientRequest.builder()
                .fullName("Dr. Test")
                .avatarUrl("/dashboard/img/default.png")
                .build();

        editPatientRequest.setFullName("Test Patient");

        when(patientService.findByUsername("patient1")).thenReturn(Optional.of(patientProfile));

        mockMvc.perform(get("/patient/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/settings"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attributeExists("specializations"));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldUpdatePatientProfile() throws Exception {
        EditPatientRequest editPatientRequest = EditPatientRequest.builder()
                .fullName("Dr. Test")
                .avatarUrl("/dashboard/img/default.png")
                .build();
        editPatientRequest.setFullName("Updated Patient");

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatarFile",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        String imageUrl = "/dashboard/img/test.jpg";
        when(userService.storeImage(any(MultipartFile.class))).thenReturn(imageUrl);

        mockMvc.perform(multipart("/patient/settings")
                        .file(avatarFile)
                        .flashAttr("editPatientRequest", editPatientRequest))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/dashboard"));

        verify(patientService).updatePatientProfile(eq("patient1"), any(EditPatientRequest.class));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldListAvailableDoctors() throws Exception {
        List<DoctorProfile> doctors = Collections.singletonList(doctorProfile);
        when(doctorProfileService.findDoctors(Specialization.CARDIOLOGY, 3)).thenReturn(doctors);

        mockMvc.perform(get("/patient/doctor")
                        .param("specialization", "CARDIOLOGY")
                        .param("minReviews", "3"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/doctor/doctor-list"))
                .andExpect(model().attribute("doctors", doctors))
                .andExpect(model().attributeExists("specializations"));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldViewDoctorDetails() throws Exception {
        UUID doctorId = doctorProfile.getId();
        when(doctorProfileService.getDoctorById(doctorId)).thenReturn(doctorProfile);
        when(userService.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(patientService.findByUser(user)).thenReturn(Optional.of(patientProfile));

        List<Review> reviews = new ArrayList<>();
        when(reviewService.getDoctorReviews(doctorId)).thenReturn(reviews);

        mockMvc.perform(get("/patient/doctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/doctor/doctor-details"))
                .andExpect(model().attribute("doctor", doctorProfile))
                .andExpect(model().attribute("reviews", reviews))
                .andExpect(model().attribute("patient", patientProfile));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldRescheduleAppointment() throws Exception {
        UUID appointmentId = appointment.getId();
        LocalDate newDate = LocalDate.now().plusDays(5);
        LocalTime newTime = LocalTime.of(14, 30);

        doNothing().when(appointmentService).rescheduleAppointment(appointmentId, newDate, newTime);

        mockMvc.perform(post("/patient/appointments/{appointmentId}/reschedule", appointmentId)
                        .param("newDate", newDate.toString())
                        .param("newTime", newTime.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"));

        verify(appointmentService).rescheduleAppointment(appointmentId, newDate, newTime);
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldGetAppointmentDetails() throws Exception {
        UUID appointmentId = appointment.getId();
        when(appointmentService.getAppointmentById(appointmentId)).thenReturn(appointment);

        mockMvc.perform(get("/patient/appointments/{appointmentId}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/appointments/appointment-details"))
                .andExpect(model().attribute("appointment", appointment));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldHandleAppointmentNotFound() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        when(appointmentService.getAppointmentById(appointmentId)).thenThrow(new NoSuchElementException());

        mockMvc.perform(get("/patient/appointments/{appointmentId}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(view().name("error/error"))
                .andExpect(model().attribute("error", "Appointment not found."))
                .andExpect(model().attribute("errorCode", 404));
    }

    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void shouldListMedicalHistory() throws Exception {
        when(userService.findByUsername("patient1")).thenReturn(Optional.of(user));
        when(patientService.findByUser(user)).thenReturn(Optional.of(patientProfile));

        List<Prescription> prescriptions = new ArrayList<>();
        when(prescriptionService.getPrescriptionsForPatient(patientProfile.getId())).thenReturn(prescriptions);

        List<MedicalRecord> records = new ArrayList<>();
        when(medicalRecordService.getRecordsForPatient(patientProfile.getId())).thenReturn(records);

        mockMvc.perform(get("/patient/medical-history"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/medical-history"))
                .andExpect(model().attribute("prescriptions", prescriptions))
                .andExpect(model().attribute("medicalRecords", records));
    }
}