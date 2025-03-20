package com.optimed.mapper;

import com.optimed.dto.EditPatientRequest;
import com.optimed.entity.PatientProfile;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PatientMapper {
    public EditPatientRequest mapToEditPatient(PatientProfile patientProfile) {
        return EditPatientRequest.builder()
                .fullName(patientProfile.getFullName())
                .address (patientProfile.getAddress())
                .phoneNumber (patientProfile.getPhoneNumber())
                .dateOfBirth (patientProfile.getDateOfBirth())
                .medicalHistory (patientProfile.getMedicalHistory())
                .email(patientProfile.getUser().getEmail())
                .avatarUrl(patientProfile.getAvatarUrl())
                .build();
    }
}
