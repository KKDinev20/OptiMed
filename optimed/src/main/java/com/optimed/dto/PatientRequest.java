package com.optimed.dto;

import com.optimed.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class PatientRequest {
    @NotBlank
    private String fullName;

    private MultipartFile avatarFile;

    private String address;
    private String email;
    private Gender gender;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String medicalHistory;

    private String avatarUrl;
}