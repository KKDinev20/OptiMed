package com.optimed.service;

import com.optimed.dto.PrescriptionRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public List<Prescription> getPrescriptionsForPatient(UUID patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public void createPrescription(PrescriptionRequest request) {
        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicationDetails(request.getMedicationDetails());
        prescription.setDosageInstructions(request.getDosageInstructions());
        prescription.setDateIssued(LocalDate.now());

        prescriptionRepository.save(prescription);
    }
}

