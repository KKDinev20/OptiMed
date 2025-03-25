package com.optimed.service;

import com.optimed.dto.PrescriptionRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrescriptionServiceTests {
    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientProfileRepository;

    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private UUID patientId;
    private UUID doctorId;
    private PatientProfile patientProfile;
    private DoctorProfile doctorProfile;
    private Prescription prescription;
    private PrescriptionRequest prescriptionRequest;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patientProfile = PatientProfile.builder()
                .id(patientId)
                .fullName("John Doe")
                .build();

        doctorProfile = DoctorProfile.builder()
                .id(doctorId)
                .fullName("Dr. Smith")
                .build();

        prescription = Prescription.builder()
                .id(UUID.randomUUID())
                .patient(patientProfile)
                .doctor(doctorProfile)
                .medicationDetails("Medication Diagnosis")
                .dosageInstructions("Take twice daily")
                .dateIssued(LocalDate.now())
                .build();

        prescriptionRequest = PrescriptionRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .medicationDetails("Medication Details")
                .dosageInstructions("Dosage Instructions")
                .build();
    }

    @Test
    void testGetPrescriptionsForPatient_WhenPatientExists_ReturnsPrescriptions() {
        // Given
        List<Prescription> prescriptions = Collections.singletonList(prescription);
        when(prescriptionRepository.findByPatientId(patientId)).thenReturn(prescriptions);

        // When
        List<Prescription> result = prescriptionService.getPrescriptionsForPatient(patientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(prescription);
        verify(prescriptionRepository).findByPatientId(patientId);
    }

    @Test
    void testGetPrescriptionsForPatient_WhenNoPrescriptionsExist_ReturnsEmptyList() {
        // Given
        when(prescriptionRepository.findByPatientId(patientId)).thenReturn(Collections.emptyList());

        // When
        List<Prescription> result = prescriptionService.getPrescriptionsForPatient(patientId);

        // Then
        assertThat(result).isEmpty();
        verify(prescriptionRepository).findByPatientId(patientId);
    }

    @Test
    void testCreatePrescription_WithValidRequest_CreatesSuccessfully() {
        // Given
        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patientProfile));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.of(doctorProfile));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        // When
        prescriptionService.createPrescription(prescriptionRequest);

        // Then
        verify(patientProfileRepository).findById(patientId);
        verify(doctorProfileRepository).findById(doctorId);
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    void testCreatePrescription_WithInvalidPatientId_ThrowsIllegalArgumentException() {
        // Given
        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> prescriptionService.createPrescription(prescriptionRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient not found");

        verify(patientProfileRepository).findById(patientId);
        verify(doctorProfileRepository, never()).findById(any());
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    void testCreatePrescription_WithInvalidDoctorId_ThrowsIllegalArgumentException() {
        // Given
        when(patientProfileRepository.findById(patientId)).thenReturn(Optional.of(patientProfile));
        when(doctorProfileRepository.findById(doctorId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> prescriptionService.createPrescription(prescriptionRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Doctor not found");

        verify(patientProfileRepository).findById(patientId);
        verify(doctorProfileRepository).findById(doctorId);
        verify(prescriptionRepository, never()).save(any());
    }
}