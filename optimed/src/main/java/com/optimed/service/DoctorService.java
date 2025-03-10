package com.optimed.service;

import com.optimed.entity.DoctorProfile;
import com.optimed.entity.PatientProfile;
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
}
