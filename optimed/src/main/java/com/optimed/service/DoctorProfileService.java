package com.optimed.service;

import com.optimed.entity.DoctorProfile;
import com.optimed.entity.enums.Specialization;
import com.optimed.repository.DoctorProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;

    public List<DoctorProfile> findDoctorsBySpecialization(Specialization specialization) {
        return doctorProfileRepository.findBySpecialization(specialization);
    }

    public List<DoctorProfile> findDoctors(Specialization specialization, Integer minReviews) {
        if (specialization != null && minReviews != null) {
            return doctorProfileRepository.findBySpecializationAndReviewsGreaterThanEqual(specialization, minReviews);
        } else if (specialization != null) {
            return doctorProfileRepository.findBySpecialization(specialization);
        } else if (minReviews != null) {
            return doctorProfileRepository.findByReviewsGreaterThanEqual(minReviews);
        } else {
            return doctorProfileRepository.findAll();
        }
    }

    public boolean doesDoctorHaveTimeSlot(UUID doctorId, LocalTime timeSlot) {
        Optional<DoctorProfile> doctor = doctorProfileRepository.findById(doctorId);

        return doctor.map(profile -> profile.getAvailableTimeSlots()
                        .stream()
                        .anyMatch(slot -> !timeSlot.isBefore(slot.getStartTime()) && !timeSlot.isAfter(slot.getEndTime())))
                .orElse(false);
    }



    public DoctorProfile getDoctorById(UUID doctorId) {
        return doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
    }
}
