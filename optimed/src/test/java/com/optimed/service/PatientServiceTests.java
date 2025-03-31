package com.optimed.service;

import com.optimed.dto.EditPatientRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.Specialization;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PatientServiceTests {
    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PatientService patientService;

    private User user;
    private PatientProfile patient;
    private DoctorProfile doctor;
    private EditPatientRequest editRequest;
    private List<Appointment> appointments;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        doctor = DoctorProfile.builder()
                .id(UUID.randomUUID())
                .fullName("Dr. Smith")
                .specialization(Specialization.CARDIOLOGY)
                .email("doc@example.com")
                .build();

        patient = PatientProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .fullName("Test Patient")
                .address("Test Address")
                .phoneNumber("123-456-7890")
                .dateOfBirth(LocalDate.now().minusYears(30))
                .medicalHistory("No known conditions")
                .email("patient@example.com")
                .build();

        editRequest = EditPatientRequest.builder()
                .fullName("Updated Patient")
                .address("Updated Address")
                .phoneNumber("098-765-4321")
                .dateOfBirth(LocalDate.now().minusYears(30))
                .medicalHistory("Updated medical history")
                .email("updated@example.com")
                .avatarUrl("new-avatar-url")
                .build();

        appointments = List.of(
                Appointment.builder()
                        .id(UUID.randomUUID())
                        .patient(patient)
                        .doctor(doctor)
                        .appointmentDate(LocalDate.now())
                        .build()
        );
    }

    @Test
    void shouldCountPatients() {
        when(patientRepository.count()).thenReturn(100L);

        long count = patientService.countPatients();

        assertEquals(100L, count);
        verify(patientRepository).count();
    }

    @Test
    void shouldGetPatientByUsername() {
        when(patientRepository.findByUserUsername("testuser"))
                .thenReturn(Optional.of(patient));

        Optional<PatientProfile> result = patientService.getPatientByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals(patient, result.get());
        verify(patientRepository).findByUserUsername("testuser");
    }

    @Test
    void shouldFindByUser() {
        when(patientRepository.findByUser(user))
                .thenReturn(Optional.of(patient));

        Optional<PatientProfile> result = patientService.findByUser(user);

        assertTrue(result.isPresent());
        assertEquals(patient, result.get());
        verify(patientRepository).findByUser(user);
    }

    @Test
    void shouldGetPatientById() {
        when(patientRepository.findById(patient.getId()))
                .thenReturn(Optional.of(patient));

        PatientProfile result = patientService.getPatientById(patient.getId());

        assertEquals(patient, result);
        verify(patientRepository).findById(patient.getId());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenPatientNotFound() {
        when(patientRepository.findById(UUID.randomUUID()))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> patientService.getPatientById(UUID.randomUUID())
        );

        assertEquals("Patient not found", exception.getMessage());
        verify(patientRepository).findById(any(UUID.class));
    }

    @Test
    void shouldUpdatePatientProfile() {
        when(patientRepository.findByUserUsername("testuser"))
                .thenReturn(Optional.of(patient));

        patientService.updatePatientProfile("testuser", editRequest);

        verify(patientRepository).findByUserUsername("testuser");
        verify(patientRepository).save(any(PatientProfile.class));

        // Verify updated fields
        assertEquals(editRequest.getFullName(), patient.getFullName());
        assertEquals(editRequest.getAddress(), patient.getAddress());
        assertEquals(editRequest.getPhoneNumber(), patient.getPhoneNumber());
        assertEquals(editRequest.getDateOfBirth(), patient.getDateOfBirth());
        assertEquals(editRequest.getMedicalHistory(), patient.getMedicalHistory());
        assertEquals(editRequest.getEmail(), patient.getEmail());
        assertEquals(editRequest.getAvatarUrl(), patient.getAvatarUrl());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenUpdatingNonExistentPatient() {
        when(patientRepository.findByUserUsername("nonexistent"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> patientService.updatePatientProfile("nonexistent", editRequest)
        );

        assertEquals("Doctor not found", exception.getMessage());
        verify(patientRepository).findByUserUsername("nonexistent");
    }

    @Test
    void shouldGetRecentPatients() {
        when(appointmentRepository.findTop5ByDoctorEmailOrderByAppointmentDateDesc("doctor@example.com"))
                .thenReturn(appointments);

        List<PatientProfile> recentPatients = patientService.getRecentPatients("doctor@example.com");

        assertEquals(1, recentPatients.size());
        assertEquals(patient, recentPatients.get(0));
        verify(appointmentRepository).findTop5ByDoctorEmailOrderByAppointmentDateDesc("doctor@example.com");
    }
}
