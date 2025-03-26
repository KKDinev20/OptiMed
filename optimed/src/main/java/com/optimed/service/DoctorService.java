package com.optimed.service;

import com.optimed.client.NotificationClient;
import com.optimed.dto.EditDoctorRequest;
import com.optimed.entity.*;
import com.optimed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationClient notificationClient;


    public long countDoctors () {
        return doctorRepository.count ();
    }

    public Optional<DoctorProfile> findByUsername(String username) {
        return doctorRepository.findByUserUsername(username);
    }

    public String getDoctorFullName(String username) {
        return findByUsername(username)
                .map(DoctorProfile::getFullName)
                .orElseThrow(() -> new RuntimeException("Doctor not found for username: " + username));
    }

    public void updateDoctorProfile(String username, EditDoctorRequest request) {
        DoctorProfile doctor = doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setFullName(request.getFullName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setAvailableDays(request.getAvailableDays() != null ? new HashSet<> (request.getAvailableDays()) : new HashSet<>());
        doctor.setContactInfo(request.getContactInfo());

        if (request.getAvatarUrl() != null) {
            doctor.setAvatarUrl(request.getAvatarUrl());
        }

        doctorRepository.save(doctor);
    }


    @Scheduled(cron = "0 0 8 * * ?")
    public void notifyDoctorsOfUpcomingAppointments() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        List<Appointment> appointments = appointmentRepository.findByAppointmentDate(tomorrow.toLocalDate());

        for (Appointment appointment : appointments) {
            notificationClient.sendNotification(appointment.getDoctor().getEmail(),
                    "Reminder: You have an appointment tomorrow at " + appointment.getAppointmentTime());
        }
    }
}
