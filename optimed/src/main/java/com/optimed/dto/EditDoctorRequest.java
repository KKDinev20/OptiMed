package com.optimed.dto;

import com.optimed.entity.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class EditDoctorRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    private String fullName;

    @NotNull(message = "Specialization is required")
    private Specialization specialization;

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private int experienceYears;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private MultipartFile avatarFile;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Bio is required")
    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    @NotBlank(message = "Available days cannot be empty")
    private String availableDays;

    @Pattern(regexp = "([01]?[0-9]|2[0-3]):[0-5][0-9]", message = "Invalid time format (HH:mm)")
    private String startTime;

    @Pattern(regexp = "([01]?[0-9]|2[0-3]):[0-5][0-9]", message = "Invalid time format (HH:mm)")
    private String endTime;

    @Size(max = 255, message = "Contact info cannot exceed 255 characters")
    private String contactInfo;

    private String avatarUrl;
}
