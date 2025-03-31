package com.optimed.service;

import com.optimed.client.NotificationClient;
import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.*;
import com.optimed.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTests {
    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AppointmentService appointmentService;

    private User user;
    private UUID doctorId;
    private UUID patientId;
    private UUID appointmentId;
    private DoctorProfile doctor;
    private PatientProfile patient;
    private Appointment appointment;
    private AppointmentRequest appointmentRequest;
    private List<TimeSlot> timeSlots;


    @BeforeEach
    void setUp() {
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

        timeSlots = List.of(
                TimeSlot.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()
        );
        doctor = DoctorProfile.builder()
                .id(doctorId)
                .user (user)
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

        appointmentRequest = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .reason("General Checkup")
                .build();

        appointment = Appointment.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .build();
    }

    @Test
    void shouldCountAppointments() {
        when(appointmentRepository.count()).thenReturn(10L);
        long count = appointmentService.countAppointments();
        assertThat(count).isEqualTo(10L);
        verify(appointmentRepository).count();
    }

    @Test
    void shouldGetAppointmentsByPatientId() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Appointment> mockPage = new PageImpl<>(List.of(appointment));
        when(appointmentRepository.findAppointmentsByFilters(patientId, null, null, null, null, pageable))
                .thenReturn(mockPage);
        Page<Appointment> result = appointmentService.getAppointmentsByPatientId(patientId, pageable, null, null, null, null);
        assertThat(result.getContent()).contains(appointment);
        verify(appointmentRepository).findAppointmentsByFilters(patientId, null, null, null, null, pageable);
    }

    @Test
    void shouldCheckAppointmentSlotAvailability() {
        when(appointmentRepository.countByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime(), AppointmentStatus.CONFIRMED))
                .thenReturn(0);
        boolean available = appointmentService.isAppointmentSlotAvailable(doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime());
        assertThat(available).isTrue();
    }

    @Test
    void shouldCreateAppointment() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime()))
                .thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
                patientId, doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime()))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        appointmentService.createAppointment(appointmentRequest);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowWhenDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.createAppointment(appointmentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Doctor or Patient not found.");
    }

    @Test
    void shouldThrowWhenPatientNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> appointmentService.createAppointment(appointmentRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Doctor or Patient not found.");
    }

    @Test
    void shouldCancelAppointment() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        appointmentService.cancelAppointment(appointmentId);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        verify(appointmentRepository).save(appointment);
        verify(notificationClient, times(2)).sendNotification(anyString(), anyString());
    }

    @Test
    void shouldApproveAppointment() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        appointmentService.approveAppointment(appointmentId);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldThrowWhenApprovingNonBookedAppointment() {
        appointment.setStatus(AppointmentStatus.CANCELED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        assertThatThrownBy(() -> appointmentService.approveAppointment(appointmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only pending appointments can be approved.");
    }

    @Test
    void shouldDeleteAppointment() {
        doNothing().when(appointmentRepository).deleteById(appointmentId);
        appointmentService.deleteAppointment(appointmentId);
        verify(appointmentRepository).deleteById(appointmentId);
    }

    @Test
    void shouldGetNextAppointment() {
        when(appointmentRepository.findTopByPatientIdOrderByAppointmentDateAsc(patientId))
                .thenReturn(appointment);
        Appointment nextAppointment = appointmentService.getNextAppointment(patientId);
        assertThat(nextAppointment).isNotNull();
        assertThat(nextAppointment.getPatient().getId()).isEqualTo(patientId);
        verify(appointmentRepository).findTopByPatientIdOrderByAppointmentDateAsc(patientId);
    }

    @Test
    void shouldGetRecentAppointments() {
        when(appointmentRepository.findTop5ByPatientIdOrderByAppointmentDateDesc(patientId))
                .thenReturn(List.of(appointment));
        List<Appointment> recentAppointments = appointmentService.getRecentAppointments(patientId);
        assertThat(recentAppointments).hasSize(1);
        assertThat(recentAppointments.get(0).getPatient().getId()).isEqualTo(patientId);
        verify(appointmentRepository).findTop5ByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    @Test
    void shouldGetRecentAppointmentsByDefault() {
        when(appointmentRepository.findTop3ByOrderByIdDesc())
                .thenReturn(List.of(appointment));
        List<Appointment> recentAppointments = appointmentService.getRecentAppointments();
        assertThat(recentAppointments).hasSize(1);
        verify(appointmentRepository).findTop3ByOrderByIdDesc();
    }

    @Test
    void shouldCountCanceledAppointments() {
        when(appointmentRepository.countByStatus(AppointmentStatus.CANCELED)).thenReturn(5L);
        long count = appointmentService.getCanceledAppointments();
        assertThat(count).isEqualTo(5L);
        verify(appointmentRepository).countByStatus(AppointmentStatus.CANCELED);
    }

    @Test
    void shouldCountBookedAppointments() {
        when(appointmentRepository.countByStatus(AppointmentStatus.BOOKED)).thenReturn(3L);
        long count = appointmentService.getBookedAppointments();
        assertThat(count).isEqualTo(3L);
        verify(appointmentRepository).countByStatus(AppointmentStatus.BOOKED);
    }

    @Test
    void shouldGetTodaysAppointments() {
        LocalDate today = LocalDate.now();
        when(appointmentRepository.countByAppointmentDate(today)).thenReturn(2L);
        long count = appointmentService.getTodaysAppointments();
        assertThat(count).isEqualTo(2L);
        verify(appointmentRepository).countByAppointmentDate(today);
    }

    @Test
    void shouldUpdateAppointmentStatus() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldGetPatientsByDoctor() {
        when(appointmentRepository.findPatientsByDoctor(doctorId))
                .thenReturn(List.of(patient));
        List<PatientProfile> patients = appointmentService.getPatientsByDoctor(doctorId);
        assertThat(patients).hasSize(1);
        assertThat(patients.get(0).getId()).isEqualTo(patientId);
        verify(appointmentRepository).findPatientsByDoctor(doctorId);
    }

    @Test
    void shouldCheckDoctorAvailability() {
        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime()))
                .thenReturn(Collections.emptyList());
        boolean available = appointmentService.isDoctorAvailable(
                doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime());
        assertThat(available).isTrue();
        verify(appointmentRepository).findByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId, appointmentRequest.getAppointmentDate(), appointmentRequest.getAppointmentTime());
    }

    @Test
    void shouldGetAppointmentById() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        Appointment result = appointmentService.getAppointmentById(appointmentId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(appointmentId);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void shouldSearchAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> mockPage = new PageImpl<>(List.of(appointment));
        when(appointmentRepository.searchAppointments(
                doctorId, null, null, null, null, pageable))
                .thenReturn(mockPage);
        Page<Appointment> result = appointmentService.searchAppointments(
                doctorId, null, null, null, null, pageable);
        assertThat(result.getContent()).contains(appointment);
        verify(appointmentRepository).searchAppointments(
                doctorId, null, null, null, null, pageable);
    }

    @Test
    void shouldRescheduleAppointment() {
        // Given
        UUID appointmentId = UUID.randomUUID();
        LocalDate newDate = LocalDate.now().plusDays(2);
        LocalTime newTime = LocalTime.of(11, 0);

        Appointment existingAppointment = Appointment.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now())
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .build();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existingAppointment));

        // When
        appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);

        // Then
        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appointmentArgumentCaptor.capture());

        Appointment updatedAppointment = appointmentArgumentCaptor.getValue();
        assertThat(updatedAppointment.getAppointmentDate()).isEqualTo(newDate);
        assertThat(updatedAppointment.getAppointmentTime()).isEqualTo(newTime);
        assertThat(updatedAppointment.getStatus()).isEqualTo(AppointmentStatus.RESCHEDULED);

        verify(notificationClient).sendNotification(
                eq(existingAppointment.getDoctor().getEmail()),
                contains(String.format("Your appointment with %s has been rescheduled to %s at %s.",
                        existingAppointment.getPatient().getFullName(), newDate, newTime)));
    }

    @Test
    void shouldNotifyDoctorsAboutUpcomingAppointments() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Appointment upcomingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(tomorrow)
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .build();

        when(appointmentRepository.findByAppointmentDateBetween(eq(tomorrow), eq(tomorrow)))
                .thenReturn(List.of(upcomingAppointment));

        // When
        appointmentService.notifyDoctorsAboutUpcomingAppointments();

        // Then
        verify(notificationClient).sendNotification(
                eq(upcomingAppointment.getDoctor().getEmail()),
                contains(String.format("Reminder: You have an appointment with %s on %s",
                        upcomingAppointment.getPatient().getFullName(), tomorrow)));
    }

    @Test
    void shouldGetUpcomingAppointmentsForMonth() {
        // Given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Appointment upcomingAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(tomorrow)
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .build();

        when(appointmentRepository.findByAppointmentDateBetween(eq(tomorrow), eq(tomorrow)))
                .thenReturn(List.of(upcomingAppointment));

        // When
        List<Appointment> appointments = appointmentService.getUpcomingAppointmentsForMonth();

        // Then
        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0)).isEqualTo(upcomingAppointment);
        verify(appointmentRepository).findByAppointmentDateBetween(eq(tomorrow), eq(tomorrow));
    }

    @Test
    void shouldCountAppointmentsByStatus() {
        // Given
        AppointmentStatus status = AppointmentStatus.BOOKED;
        long expectedCount = 5L;

        when(appointmentRepository.countByStatus(status)).thenReturn(expectedCount);

        // When
        long actualCount = appointmentService.countAppointmentsByStatus(status);

        // Then
        assertThat(actualCount).isEqualTo(expectedCount);
        verify(appointmentRepository).countByStatus(status);
    }

    @Test
    void shouldGetAllAppointments() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now())
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.BOOKED)
                .build();

        Page<Appointment> mockPage = new PageImpl<>(List.of(appointment));

        when(appointmentRepository.findAll(eq(pageable))).thenReturn(mockPage);

        // When
        Page<Appointment> result = appointmentService.getAllAppointments(pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(appointment);
        verify(appointmentRepository).findAll(eq(pageable));
    }

    @Test
    void shouldCountAppointmentsByStatusMap() {
        // Given
        long canceledCount = 2L;
        long bookedCount = 3L;
        long confirmedCount = 1L;

        when(appointmentRepository.countByStatus(AppointmentStatus.CANCELED)).thenReturn(canceledCount);
        when(appointmentRepository.countByStatus(AppointmentStatus.BOOKED)).thenReturn(bookedCount);
        when(appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED)).thenReturn(confirmedCount);

        // When
        Map<String, Long> counts = appointmentService.countAppointmentsByStatus();

        // Then
        assertThat(counts.get("Canceled")).isEqualTo(canceledCount);
        assertThat(counts.get("Booked")).isEqualTo(bookedCount);
        assertThat(counts.get("Confirmed")).isEqualTo(confirmedCount);

        verify(appointmentRepository).countByStatus(AppointmentStatus.CANCELED);
        verify(appointmentRepository).countByStatus(AppointmentStatus.BOOKED);
        verify(appointmentRepository).countByStatus(AppointmentStatus.CONFIRMED);
    }
}