package com.optimed.web.mapper;

import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.DoctorProfile;
import com.optimed.entity.enums.Specialization;
import com.optimed.mapper.DoctorMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DoctorMapperTest {

    @Test
    void shouldMapDoctorProfileToEditDoctorRequest() {
        DoctorProfile doctor = new DoctorProfile();
        doctor.setFullName("Dr. John Doe");
        doctor.setSpecialization(Specialization.CARDIOLOGY);
        doctor.setAvatarUrl("https://example.com/avatar.jpg");

        EditDoctorRequest dto = DoctorMapper.mapToEditDoctorRequest(doctor);

        assertThat(dto.getFullName()).isEqualTo("Dr. John Doe");
        assertThat(dto.getSpecialization()).isEqualTo(Specialization.CARDIOLOGY);
        assertThat(dto.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
    }
}
