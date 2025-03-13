package com.optimed.service;

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
}
