package com.optimed.service;

import com.optimed.client.NotificationClient;
import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.entity.enums.Specialization;
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

public class AppointmentServiceTests {
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

    private UUID doctorId, patientId, appointmentId;
    private DoctorProfile doctor;
    private PatientProfile patient;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        doctor = DoctorProfile.builder().id(doctorId).fullName("Dr. Smith").build();
        patient = PatientProfile.builder().id(patientId).fullName("John Doe").email("john@example.com").build();

        appointment = Appointment.builder()
                .id(appointmentId)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.CANCELED)
                .reason("Checkup")
                .build();
    }

    @Test
    void shouldReturnAppointmentCount() {
        when(appointmentRepository.count()).thenReturn(5L);

        long count = appointmentService.countAppointments();

        assertThat(count).isEqualTo(5L);
        verify(appointmentRepository).count();
    }

    @Test
    void shouldGetAppointmentsByPatientId() {
        Pageable pageable = PageRequest.of(0, 10);

        when(appointmentRepository.findAppointmentsByFilters(
                patientId,
                "Smith",
                AppointmentStatus.BOOKED,
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                pageable))
                .thenReturn(Page.empty());

        Page<Appointment> appointments = appointmentService.getAppointmentsByPatientId(
                patientId,
                pageable,
                "Smith",
                AppointmentStatus.BOOKED,
                LocalDate.now(),
                LocalDate.now().plusMonths(1));

        assertThat(appointments).isNotNull();
        assertThat(appointments.getContent()).isEmpty();
        verify(appointmentRepository).findAppointmentsByFilters(
                patientId,
                "Smith",
                AppointmentStatus.BOOKED,
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                pageable);
    }


    @Test
    void shouldRescheduleAppointmentSuccessfully() {
        LocalDate newDate = LocalDate.now().plusDays(3);
        LocalTime newTime = LocalTime.of(15, 0);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);

        assertThat(appointment.getAppointmentDate()).isEqualTo(newDate);
        assertThat(appointment.getAppointmentTime()).isEqualTo(newTime);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.RESCHEDULED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldCancelAppointmentSuccessfully() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        appointmentService.cancelAppointment(appointmentId);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        verify(notificationClient).sendNotification(eq(patient.getEmail()), anyString());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldThrowExceptionWhenDoctorIsBooked() {
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .specialization(Specialization.CARDIOLOGY)
                .reason("General Checkup")
                .build();


        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId,
                request.getAppointmentDate(),
                request.getAppointmentTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("This doctor is already booked");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldGetAllAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> page = Page.empty();

        when(appointmentRepository.findAll(pageable)).thenReturn(page);

        Page<Appointment> appointments = appointmentService.getAllAppointments(pageable);

        assertThat(appointments).isNotNull();
        assertThat(appointments.getContent()).isEmpty();
        verify(appointmentRepository).findAll(pageable);
    }

    @Test
    void shouldGetCanceledAppointments() {
        when(appointmentRepository.countByStatus(AppointmentStatus.CANCELED))
                .thenReturn(5L);

        long count = appointmentService.getCanceledAppointments();

        assertThat(count).isEqualTo(5L);
        verify(appointmentRepository).countByStatus(AppointmentStatus.CANCELED);
    }

    @Test
    void shouldGetBookedAppointments() {
        when(appointmentRepository.countByStatus(AppointmentStatus.BOOKED))
                .thenReturn(3L);

        long count = appointmentService.getBookedAppointments();

        assertThat(count).isEqualTo(3L);
        verify(appointmentRepository).countByStatus(AppointmentStatus.BOOKED);
    }

    @Test
    void shouldGetTodaysAppointments() {
        LocalDate today = LocalDate.now();
        when(appointmentRepository.countByAppointmentDate(today))
                .thenReturn(2L);

        long count = appointmentService.getTodaysAppointments();

        assertThat(count).isEqualTo(2L);
        verify(appointmentRepository).countByAppointmentDate(today);
    }

    @Test
    void shouldGetUpcomingAppointmentsForMonth() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        Page<Appointment> page = Page.empty();
        when(appointmentRepository.findByAppointmentDateBetween(
                firstDayOfMonth,
                lastDayOfMonth,
                pageable))
                .thenReturn(page);

        Page<Appointment> appointments = appointmentService.getUpcomingAppointmentsForMonth(pageable);

        assertThat(appointments).isNotNull();
        assertThat(appointments.getContent()).isEmpty();
        verify(appointmentRepository).findByAppointmentDateBetween(
                firstDayOfMonth,
                lastDayOfMonth,
                pageable);
    }

    @Test
    void shouldSearchAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID doctorId = UUID.randomUUID();

        Page<Appointment> page = Page.empty();
        when(appointmentRepository.searchAppointments(
                doctorId,
                AppointmentStatus.BOOKED,
                "John Doe",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                pageable))
                .thenReturn(page);

        Page<Appointment> appointments = appointmentService.searchAppointments(
                doctorId,
                AppointmentStatus.BOOKED,
                "John Doe",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                pageable);

        assertThat(appointments).isNotNull();
        assertThat(appointments.getContent()).isEmpty();
        verify(appointmentRepository).searchAppointments(
                doctorId,
                AppointmentStatus.BOOKED,
                "John Doe",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                pageable);
    }

    @Test
    void shouldGetRecentAppointments() {
        List<Appointment> recent = Arrays.asList(appointment);
        when(appointmentRepository.findTop3ByOrderByIdDesc())
                .thenReturn(recent);

        List<Appointment> appointments = appointmentService.getRecentAppointments();

        assertThat(appointments).containsExactly(appointment);
        verify(appointmentRepository).findTop3ByOrderByIdDesc();
    }

    @Test
    void shouldUpdateAppointmentStatus() {
        AppointmentStatus newStatus = AppointmentStatus.CONFIRMED;
        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        appointmentService.updateAppointmentStatus(appointmentId, newStatus);

        assertThat(appointment.getStatus()).isEqualTo(newStatus);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldGetPatientsByDoctor() {
        List<PatientProfile> patients = Arrays.asList(patient);

        when(appointmentRepository.findPatientsByDoctor(doctorId))
                .thenReturn(patients);

        List<PatientProfile> result = appointmentService.getPatientsByDoctor(doctorId);

        assertThat(result).containsExactly(patient);
        verify(appointmentRepository).findPatientsByDoctor(doctorId);
    }

    @Test
    void shouldCheckDoctorAvailability() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        when(appointmentRepository.findByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId,
                date,
                time))
                .thenReturn(Collections.emptyList());

        boolean available = appointmentService.isDoctorAvailable(
                doctorId,
                date,
                time);

        assertThat(available).isTrue();
        verify(appointmentRepository).findByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId,
                date,
                time);
    }

    @Test
    void shouldGetAppointmentById() {
        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        Appointment result = appointmentService.getAppointmentById(appointmentId);

        assertThat(result).isEqualTo(appointment);
        verify(appointmentRepository).findById(appointmentId);
    }

    @Test
    void shouldDeleteAppointment() {
        UUID id = UUID.randomUUID();

        appointmentService.deleteAppointment(id);

        verify(appointmentRepository).deleteById(id);
    }

    @Test
    void shouldCreateAppointmentSuccessfully() {
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .specialization(Specialization.CARDIOLOGY)
                .reason("General Checkup")
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId,
                request.getAppointmentDate(),
                request.getAppointmentTime()))
                .thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
                patientId,
                doctorId,
                request.getAppointmentDate(),
                request.getAppointmentTime()))
                .thenReturn(false);

        appointmentService.createAppointment(request);

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .specialization(Specialization.CARDIOLOGY)
                .reason("General Checkup")
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Doctor or Patient not found");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void shouldThrowExceptionWhenPatientAlreadyBooked() {
        AppointmentRequest request = AppointmentRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .specialization(Specialization.CARDIOLOGY)
                .reason("General Checkup")
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctorId,
                request.getAppointmentDate(),
                request.getAppointmentTime()))
                .thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
                patientId,
                doctorId,
                request.getAppointmentDate(),
                request.getAppointmentTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already booked");

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
