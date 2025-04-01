package com.optimed.mapper;

import com.optimed.dto.EditPatientRequest;
import com.optimed.entity.PatientProfile;
import com.optimed.entity.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatientMapperTest {

    @Test
    void testMapToEditPatientRequest() {
        User user = new User();
        user.setEmail("patient@example.com");

        PatientProfile patientProfile = PatientProfile.builder()
                .fullName("Jane Doe")
                .address("123 Main St, City, Country")
                .phoneNumber("987654321")
                .medicalHistory("No significant medical history")
                .user(user)
                .avatarUrl("/images/patient-avatar.jpg")
                .build();

        EditPatientRequest editPatientRequest = PatientMapper.mapToEditPatient(patientProfile);

        assertNotNull(editPatientRequest);
        assertEquals("Jane Doe", editPatientRequest.getFullName());
        assertEquals("123 Main St, City, Country", editPatientRequest.getAddress());
        assertEquals("987654321", editPatientRequest.getPhoneNumber());
        assertEquals("No significant medical history", editPatientRequest.getMedicalHistory());
        assertEquals("patient@example.com", editPatientRequest.getEmail());
        assertEquals("/images/patient-avatar.jpg", editPatientRequest.getAvatarUrl());
    }
}
