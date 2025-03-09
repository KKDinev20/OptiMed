package com.optimed.dto;

import com.optimed.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PatientRequest {
    @NotBlank
    private String fullName;

    private MultipartFile avatarFile;

    private String address;
    private Gender gender;
    private String phoneNumber;
    private String medicalHistory;

    private String avatarUrl;
}