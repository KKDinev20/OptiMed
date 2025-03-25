package com.optimed.web;

import com.optimed.dto.PrescriptionRequest;
import com.optimed.entity.Prescription;
import com.optimed.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_DOCTOR')")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @PostMapping("/prescription")
    public ResponseEntity<String> createPrescription(
            @RequestParam UUID patientId,
            @RequestParam UUID doctorId,
            @RequestParam String medicationDetails,
            @RequestParam String dosageInstructions
    ) {
        PrescriptionRequest request = new PrescriptionRequest(patientId, doctorId, medicationDetails, dosageInstructions);
        prescriptionService.createPrescription(request);
        return ResponseEntity.ok("Prescription saved successfully!");
    }

    @GetMapping("/prescriptions/{patientId}")
    public List<Prescription> getPrescriptions(@PathVariable UUID patientId) {
        return prescriptionService.getPrescriptionsForPatient(patientId);
    }
}
