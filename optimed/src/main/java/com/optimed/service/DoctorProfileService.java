package com.optimed.service;

import com.optimed.dto.DoctorRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public Optional<DoctorProfile> getDoctorByUserId(UUID userId) {
        return doctorProfileRepository.findByUserId(userId);
    }

    public List<Appointment> getAppointmentsByDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public DoctorProfile updateDoctorProfile(UUID doctorId, DoctorRequest doctorProfileRequest) {
        DoctorProfile existingDoctorProfile = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        existingDoctorProfile.setFullName(doctorProfileRequest.getFullName());
        existingDoctorProfile.setSpecialization(doctorProfileRequest.getSpecialization());
        existingDoctorProfile.setContactInfo(doctorProfileRequest.getContactInfo());

        return doctorProfileRepository.save(existingDoctorProfile);
    }
}
