package com.optimed.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionRequest {
    private UUID patientId;
    private UUID doctorId;
    private String medicationDetails;
    private String dosageInstructions;
}
