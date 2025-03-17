package com.optimed.dto;

import com.optimed.entity.enums.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DoctorRequest {
    @NotBlank
    private String fullName;

    private Specialization specialization;

    private int experienceYears;

    private String email;

    private MultipartFile avatarFile;

    private Gender gender;

    @NotBlank
    private String bio;

    @NotBlank(message = "Available days cannot be empty")
    private String availableDays;

    @NotBlank(message = "Start time cannot be empty")
    private String startTime;

    @NotBlank(message = "End time cannot be empty")
    private String endTime;

    private String contactInfo;

    private String avatarUrl;
}
