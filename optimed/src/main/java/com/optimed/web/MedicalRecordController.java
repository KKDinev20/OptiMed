package com.optimed.web;

import com.optimed.dto.MedicalRecordRequest;
import com.optimed.entity.MedicalRecord;
import com.optimed.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_DOCTOR')")
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    @PostMapping("/medical-record")
    public ResponseEntity<String> createMedicalRecord(@ModelAttribute MedicalRecordRequest request) {
        medicalRecordService.createMedicalRecord(request);
        return ResponseEntity.ok("Medical record saved successfully!");
    }

    @GetMapping("/medical-records/{patientId}")
    public List<MedicalRecord> getMedicalRecords(@PathVariable UUID patientId) {
        return medicalRecordService.getRecordsForPatient(patientId);
    }
}
