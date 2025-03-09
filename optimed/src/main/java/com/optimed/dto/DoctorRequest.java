package com.optimed.dto;

import com.optimed.entity.enums.Gender;
import com.optimed.entity.enums.Specialization;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DoctorRequest {
    @NotBlank
    private String fullName;

    private Specialization specialization;

    private int experienceYears;

    private MultipartFile avatarFile;
    private Gender gender;


    @NotBlank
    private String bio;

    @NotBlank
    private String availabilitySchedule;

    private String contactInfo;

    private String avatarUrl;
}
