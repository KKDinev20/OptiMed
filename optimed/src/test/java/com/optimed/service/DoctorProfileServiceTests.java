package com.optimed.service;

import com.optimed.entity.*;
import com.optimed.entity.enums.Specialization;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorProfileServiceTests {
    @Mock
    private DoctorProfileRepository doctorProfileRepository;

    @InjectMocks
    private DoctorProfileService doctorProfileService;

    private UUID doctorId;
    private DoctorProfile doctor;
    private List<TimeSlot> timeSlots;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        timeSlots = List.of(
                TimeSlot.builder()
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build()
        );
        doctor = DoctorProfile.builder()
                .id(doctorId)
                .fullName("Dr. Smith")
                .specialization(Specialization.CARDIOLOGY)
                .availableTimeSlots(timeSlots)
                .build();
    }

    @Test
    void shouldFindDoctorsBySpecialization() {
        when(doctorProfileRepository.findBySpecialization(Specialization.CARDIOLOGY))
                .thenReturn(List.of(doctor));

        List<DoctorProfile> doctors = doctorProfileService.findDoctorsBySpecialization(Specialization.CARDIOLOGY);

        assertThat(doctors).hasSize(1);
        assertThat(doctors.get(0)).isEqualTo(doctor);
        verify(doctorProfileRepository).findBySpecialization(Specialization.CARDIOLOGY);
    }

    @Test
    void shouldFindDoctorsWithSpecializationAndMinReviews() {
        when(doctorProfileRepository.findBySpecializationAndReviewsGreaterThanEqual(
                Specialization.CARDIOLOGY, 5))
                .thenReturn(List.of(doctor));

        List<DoctorProfile> doctors = doctorProfileService.findDoctors(
                Specialization.CARDIOLOGY, 5);

        assertThat(doctors).hasSize(1);
        assertThat(doctors.get(0)).isEqualTo(doctor);
        verify(doctorProfileRepository)
                .findBySpecializationAndReviewsGreaterThanEqual(Specialization.CARDIOLOGY, 5);
    }

    @Test
    void shouldFindDoctorsWithMinReviewsOnly() {
        when(doctorProfileRepository.findByReviewsGreaterThanEqual(5))
                .thenReturn(List.of(doctor));

        List<DoctorProfile> doctors = doctorProfileService.findDoctors(null, 5);

        assertThat(doctors).hasSize(1);
        assertThat(doctors.get(0)).isEqualTo(doctor);
        verify(doctorProfileRepository).findByReviewsGreaterThanEqual(5);
    }

    @Test
    void shouldFindAllDoctorsWhenNoFilters() {
        when(doctorProfileRepository.findAll())
                .thenReturn(List.of(doctor));

        List<DoctorProfile> doctors = doctorProfileService.findDoctors(null, null);

        assertThat(doctors).hasSize(1);
        assertThat(doctors.get(0)).isEqualTo(doctor);
        verify(doctorProfileRepository).findAll();
    }

    @Test
    void shouldCheckDoctorTimeSlotAvailability() {
        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        boolean hasTimeSlot = doctorProfileService.doesDoctorHaveTimeSlot(
                doctorId, LocalTime.of(10, 0));

        assertThat(hasTimeSlot).isTrue();
        verify(doctorProfileRepository).findById(doctorId);
    }

    @Test
    void shouldReturnFalseForTimeSlotOutsideRange() {
        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        boolean hasTimeSlot = doctorProfileService.doesDoctorHaveTimeSlot(
                doctorId, LocalTime.of(18, 0));

        assertThat(hasTimeSlot).isFalse();
        verify(doctorProfileRepository).findById(doctorId);
    }

    @Test
    void shouldReturnFalseForNonExistentDoctor() {
        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.empty());

        boolean hasTimeSlot = doctorProfileService.doesDoctorHaveTimeSlot(
                doctorId, LocalTime.of(10, 0));

        assertThat(hasTimeSlot).isFalse();
        verify(doctorProfileRepository).findById(doctorId);
    }

    @Test
    void shouldGetDoctorById() {
        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.of(doctor));

        DoctorProfile foundDoctor = doctorProfileService.getDoctorById(doctorId);

        assertThat(foundDoctor).isEqualTo(doctor);
        verify(doctorProfileRepository).findById(doctorId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDoctorNotFound() {
        when(doctorProfileRepository.findById(doctorId))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> doctorProfileService.getDoctorById(doctorId)
        );

        assertThat(exception.getMessage()).isEqualTo("Doctor not found");
        verify(doctorProfileRepository).findById(doctorId);
    }
}
