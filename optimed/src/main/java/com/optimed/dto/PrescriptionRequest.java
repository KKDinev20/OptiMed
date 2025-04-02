package com.optimed.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionRequest {

    @NotNull
    private UUID patientId;

    @NotNull
    private UUID doctorId;

    @NotEmpty(message = "Details are required")
    private String medicationDetails;

    @NotEmpty(message = "Instructions are required")
    private String dosageInstructions;
}
