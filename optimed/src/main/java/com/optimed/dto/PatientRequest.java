package com.optimed.dto;

import com.optimed.entity.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class PatientRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 50, message = "Full name must be between 2 and 50 characters")
    private String fullName;

    private MultipartFile avatarFile;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Size(max = 1000, message = "Medical history cannot exceed 1000 characters")
    private String medicalHistory;

    private String avatarUrl;
}
