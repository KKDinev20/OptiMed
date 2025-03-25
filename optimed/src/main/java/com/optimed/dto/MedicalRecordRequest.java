package com.optimed.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordRequest {
    private UUID patientId;
    private String diagnosis;
    private String treatmentPlan;
    private String notes;
}
