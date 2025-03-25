package com.optimed.service;

import com.optimed.dto.MedicalRecordRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientProfileRepository;

    public List<MedicalRecord> getRecordsForPatient(UUID patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }


    public void createMedicalRecord(MedicalRecordRequest request) {
        PatientProfile patient = patientProfileRepository.findById(request.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                .recordDate(LocalDate.now())
                .notes(request.getNotes())
                .build();

        medicalRecordRepository.save(record);
    }
}