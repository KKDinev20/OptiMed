package com.optimed.mapper;

import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.DoctorProfile;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DoctorMapper {

    public EditDoctorRequest mapToEditDoctorRequest(DoctorProfile doctorProfile) {
        return EditDoctorRequest.builder()
                .fullName(doctorProfile.getFullName())
                .specialization(doctorProfile.getSpecialization())
                .experienceYears(doctorProfile.getExperienceYears())
                .email(doctorProfile.getUser().getEmail())
                .bio(doctorProfile.getBio())
                .availableDays(doctorProfile.getAvailableDays())
                .startTime(doctorProfile.getStartTime())
                .endTime(doctorProfile.getEndTime())
                .contactInfo(doctorProfile.getContactInfo())
                .avatarUrl(doctorProfile.getAvatarUrl())
                .build();
    }
}
