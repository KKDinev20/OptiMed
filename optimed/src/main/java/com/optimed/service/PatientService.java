package com.optimed.service;

import com.optimed.entity.PatientProfile;
import com.optimed.entity.User;
import com.optimed.repository.AppointmentRepository;
import com.optimed.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
}
