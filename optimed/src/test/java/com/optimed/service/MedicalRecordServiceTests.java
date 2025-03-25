package com.optimed.service;

import com.optimed.dto.MedicalRecordRequest;
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
class MedicalRecordServiceTests {
    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PatientRepository patientProfileRepository;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private UUID patientId;
    private PatientProfile patient;
    private MedicalRecord medicalRecord;
    private MedicalRecordRequest request;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        patient = PatientProfile.builder()
                .id(patientId)
                .fullName("John Doe")
                .build();

        medicalRecord = MedicalRecord.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .diagnosis("Test Diagnosis")
                .treatmentPlan("Test Treatment")
                .recordDate(LocalDate.now())
                .notes("Test Notes")
                .build();

        request = MedicalRecordRequest.builder()
                .patientId(patientId)
                .diagnosis("Test Diagnosis")
                .treatmentPlan("Test Treatment")
                .notes("Test Notes")
                .build();
    }

    @Test
    void shouldGetRecordsForPatient() {
        // Given
        List<MedicalRecord> records = Collections.singletonList(medicalRecord);
        when(medicalRecordRepository.findByPatientId(patientId)).thenReturn(records);

        // When
        List<MedicalRecord> result = medicalRecordService.getRecordsForPatient(patientId);

        // Then
        assertThat(result).containsExactly(medicalRecord);
        verify(medicalRecordRepository).findByPatientId(patientId);
    }

    @Test
    void shouldGetEmptyListWhenNoRecordsFound() {
        // Given
        when(medicalRecordRepository.findByPatientId(patientId)).thenReturn(Collections.emptyList());

        // When
        List<MedicalRecord> result = medicalRecordService.getRecordsForPatient(patientId);

        // Then
        assertThat(result).isEmpty();
        verify(medicalRecordRepository).findByPatientId(patientId);
    }

    @Test
    void shouldCreateMedicalRecordSuccessfully() {
        // Given
        when(patientProfileRepository.findById(request.getPatientId()))
                .thenReturn(Optional.of(patient));

        // When
        medicalRecordService.createMedicalRecord(request);

        // Then
        verify(patientProfileRepository).findById(request.getPatientId());
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPatientNotFound() {
        // Given
        when(patientProfileRepository.findById(request.getPatientId()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient not found");

        verify(patientProfileRepository).findById(request.getPatientId());
        verify(medicalRecordRepository, never()).save(any(MedicalRecord.class));
    }
}