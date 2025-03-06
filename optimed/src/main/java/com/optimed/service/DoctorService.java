package com.optimed.service;

import com.optimed.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public long countDoctors () {
        return doctorRepository.count ();
    }
}
