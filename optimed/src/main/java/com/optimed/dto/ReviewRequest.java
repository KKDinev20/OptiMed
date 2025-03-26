package com.optimed.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
    @NotNull
    private UUID doctorId;

    @NotNull
    private UUID patientId;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 1000)
    private String comment;
}
