package com.optimed.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Contact info is required")
    private String contactInfo;
}
