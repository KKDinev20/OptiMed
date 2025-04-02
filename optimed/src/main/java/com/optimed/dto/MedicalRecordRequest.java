package com.optimed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordRequest {
    private UUID patientId;

    @NotBlank
    @Size(min = 3, max = 50)
    private String diagnosis;

    @NotBlank(message = "Treatment plan is required")
    @Size(max = 240)
    private String treatmentPlan;
    private String notes;
}
