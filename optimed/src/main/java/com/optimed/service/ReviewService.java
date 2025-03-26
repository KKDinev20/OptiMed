package com.optimed.service;

import com.optimed.dto.ReviewRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public void leaveReview(ReviewRequest request) {
        DoctorProfile doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        PatientProfile patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Review review = Review.builder()
                .doctor(doctor)
                .patient(patient)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);
    }

    public List<Review> getDoctorReviews(UUID doctorId) {
        return reviewRepository.findByDoctorId(doctorId);
    }
}
