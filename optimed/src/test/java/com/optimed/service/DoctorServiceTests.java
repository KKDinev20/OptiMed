package com.optimed.service;

import com.optimed.client.NotificationClient;
import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.Specialization;
import com.optimed.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTests {
    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private DoctorService doctorService;

    private DoctorProfile doctor;
    private EditDoctorRequest request;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctor = DoctorProfile.builder()
                .id(UUID.randomUUID())
                .fullName("Dr. Smith")
                .specialization(Specialization.CARDIOLOGY)
                .email("doc@example.com")
                .build();

        request = EditDoctorRequest.builder()
                .fullName("Dr. Smith Updated")
                .specialization(Specialization.CARDIOLOGY)
                .experienceYears(10)
                .bio("Cardiology specialist")
                .contactInfo("123-456-7890")
                .build();

        appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .appointmentTime(LocalTime.of(10, 0))
                .build();
    }

    @Test
    void shouldCountDoctors() {
        when(doctorRepository.count()).thenReturn(5L);

        // When
        long count = doctorService.countDoctors();

        // Then
        assertThat(count).isEqualTo(5L);
        verify(doctorRepository).count();
    }

    @Test
    void shouldFindByUsername() {
        // Given
        when(doctorRepository.findByUserUsername("testdoc")).thenReturn(Optional.of(doctor));

        // When
        Optional<DoctorProfile> result = doctorService.findByUsername("testdoc");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(doctor);
    }

    @Test
    void shouldGetDoctorFullName() {
        // Given
        when(doctorRepository.findByUserUsername("testdoc")).thenReturn(Optional.of(doctor));

        // When
        String fullName = doctorService.getDoctorFullName("testdoc");

        // Then
        assertThat(fullName).isEqualTo("Dr. Smith");
    }

    @Test
    void shouldThrowExceptionWhenDoctorNotFound() {
        // Given
        when(doctorRepository.findByUserUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> doctorService.getDoctorFullName("nonexistent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Doctor not found");
        verify(doctorRepository).findByUserUsername("nonexistent");
    }

    @Test
    void shouldUpdateDoctorProfile() {
        // Given
        when(doctorRepository.findByUserUsername("testdoc")).thenReturn(Optional.of(doctor));

        // When
        doctorService.updateDoctorProfile("testdoc", request);

        // Then
        verify(doctorRepository).findByUserUsername("testdoc");
        verify(doctorRepository).save(any(DoctorProfile.class));
    }

    @Test
    void shouldNotifyDoctorsOfUpcomingAppointments() {
        // Given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        when(appointmentRepository.findByAppointmentDate(tomorrow.toLocalDate()))
                .thenReturn(Collections.singletonList(appointment));

        // When
        doctorService.notifyDoctorsOfUpcomingAppointments();

        // Then
        verify(appointmentRepository).findByAppointmentDate(tomorrow.toLocalDate());
        verify(notificationClient).sendNotification(
                doctor.getEmail(),
                "Reminder: You have an appointment tomorrow at " + appointment.getAppointmentTime()
        );
    }
}
