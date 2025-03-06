package com.optimed.service;

import com.optimed.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public long countPatients () {
        return patientRepository.count ();
    }
}
