package com.optimed.service;

import com.optimed.dto.ReviewRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID doctorId;
    private UUID patientId;
    private UUID reviewId;
    private DoctorProfile doctor;
    private PatientProfile patient;
    private Review review;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        doctor = DoctorProfile.builder()
                .id(doctorId)
                .fullName("Dr. John Doe")
                .build();

        patient = PatientProfile.builder()
                .id(patientId)
                .fullName("John Smith")
                .build();

        reviewRequest = ReviewRequest.builder()
                .doctorId(doctorId)
                .patientId(patientId)
                .rating(5)
                .comment("Excellent doctor!")
                .build();

        review = Review.builder()
                .doctor(doctor)
                .patient(patient)
                .rating(5)
                .comment("Excellent doctor!")
                .build();
    }

    @Test
    void shouldLeaveReview() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.leaveReview(reviewRequest);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldThrowWhenDoctorNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.leaveReview(reviewRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Doctor not found");
    }

    @Test
    void shouldThrowWhenPatientNotFound() {
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.leaveReview(reviewRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Patient not found");
    }

    @Test
    void shouldGetDoctorReviews() {
        when(reviewRepository.findByDoctorId(doctorId)).thenReturn(List.of(review));

        List<Review> reviews = reviewService.getDoctorReviews(doctorId);

        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getComment()).isEqualTo("Excellent doctor!");
    }

    @Test
    void shouldDeleteReview() {
        doNothing().when(reviewRepository).deleteById(reviewId);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository).deleteById(reviewId);
    }
}
