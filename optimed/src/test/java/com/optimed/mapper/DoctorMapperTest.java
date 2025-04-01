package com.optimed.mapper;

import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.DoctorProfile;
import com.optimed.entity.User;
import com.optimed.entity.enums.Specialization;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoctorMapperTest {

    @Test
    void testMapToEditDoctorRequest() {
        User user = new User();
        user.setEmail("doctor@example.com");

        DoctorProfile doctorProfile = DoctorProfile.builder()
                .fullName("Dr. John Doe")
                .specialization(Specialization.CARDIOLOGY)
                .experienceYears(10)
                .user(user)
                .bio("Experienced cardiologist")
                .contactInfo("123456789")
                .avatarUrl("/images/avatar.jpg")
                .build();

        EditDoctorRequest editDoctorRequest = DoctorMapper.mapToEditDoctorRequest(doctorProfile);

        assertNotNull(editDoctorRequest);
        assertEquals("Dr. John Doe", editDoctorRequest.getFullName());
        assertEquals(Specialization.CARDIOLOGY, editDoctorRequest.getSpecialization());
        assertEquals(10, editDoctorRequest.getExperienceYears());
        assertEquals("doctor@example.com", editDoctorRequest.getEmail());
        assertEquals("Experienced cardiologist", editDoctorRequest.getBio());
        assertEquals("123456789", editDoctorRequest.getContactInfo());
        assertEquals("/images/avatar.jpg", editDoctorRequest.getAvatarUrl());
    }
}
