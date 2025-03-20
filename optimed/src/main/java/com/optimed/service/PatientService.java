package com.optimed.service;

import com.optimed.dto.EditDoctorRequest;
import com.optimed.dto.EditPatientRequest;
import com.optimed.entity.DoctorProfile;
import com.optimed.entity.PatientProfile;
import com.optimed.entity.User;
import com.optimed.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public long countPatients () {
        return patientRepository.count ();
    }

    public Optional<PatientProfile> getPatientByUsername(String username) {
        return patientRepository.findByUserUsername(username);
    }

    public Optional<PatientProfile> findByUser(User user) {
        return patientRepository.findByUser(user);
    }

    public PatientProfile getPatientById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException ("Patient not found"));
    }

    public Optional<PatientProfile> findByUsername(String username) {
        return patientRepository.findByUserUsername(username);
    }


    public void updatePatientProfile(String username, EditPatientRequest request) {
        PatientProfile patient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));


        patient.setFullName(request.getFullName());
        patient.setAddress (request.getAddress ());
        patient.setPhoneNumber (request.getPhoneNumber());
        patient.setDateOfBirth (request.getDateOfBirth ());
        patient.setMedicalHistory (request.getMedicalHistory ());
        patient.setEmail (request.getEmail());


        /*.fullName(patientProfile.getFullName())
                .address (patientProfile.getAddress())
                .phoneNumber (patientProfile.getPhoneNumber())
                .dateOfBirth (patientProfile.getDateOfBirth())
                .medicalHistory (patientProfile.getMedicalHistory())
                .email(patientProfile.getUser().getEmail())
                .avatarUrl(patientProfile.getAvatarUrl())
                .build();*/

        if (request.getAvatarUrl() != null) {
            patient.setAvatarUrl(request.getAvatarUrl());
        }

        patientRepository.save(patient);
    }

}
