package com.optimed.service;

import com.optimed.entity.DoctorProfile;
import com.optimed.entity.enums.Specialization;
import com.optimed.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;

    public List<DoctorProfile> findDoctorsBySpecialization(Specialization specialization) {
        return doctorProfileRepository.findBySpecialization(specialization);
    }
}
