package com.optimed.service;

import com.optimed.dto.DoctorRequest;
import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.DoctorProfile;
import com.optimed.mapper.DoctorMapper;
import com.optimed.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    public long countDoctors () {
        return doctorRepository.count ();
    }

    public Optional<DoctorProfile> findByUsername(String username) {
        return doctorRepository.findByUserUsername(username);
    }

    public String getDoctorFullName(String username) {
        return findByUsername(username)
                .map(DoctorProfile::getFullName)
                .orElseThrow(() -> new RuntimeException("Doctor not found for username: " + username));
    }

    public void updateDoctorProfile(String username, EditDoctorRequest request) {
        DoctorProfile doctor = doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setAvailableDays(request.getAvailableDays());
        doctor.setStartTime(request.getStartTime());
        doctor.setEndTime(request.getEndTime());
        doctor.setContactInfo(request.getContactInfo());

        if (request.getAvatarUrl() != null) {
            doctor.setAvatarUrl(request.getAvatarUrl());
        }

        doctorRepository.save(doctor);
    }


}
