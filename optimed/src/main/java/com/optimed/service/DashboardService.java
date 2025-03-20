package com.optimed.service;

import com.optimed.dto.DashboardStats;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalDoctors(doctorRepository.count());
        stats.setTotalPatients(patientRepository.count());
        stats.setTotalAppointments(appointmentService.countAppointments());

        Map<String, Long> statusCounts = appointmentService.countAppointmentsByStatus();

        stats.setPendingAppointments(statusCounts.getOrDefault("Pending", 0L));
        stats.setBookedAppointments(statusCounts.getOrDefault("Booked", 0L));
        stats.setConfirmedAppointments(statusCounts.getOrDefault("Confirmed", 0L));

        stats.setAppointmentsByStatus(statusCounts);

        return stats;
    }

}
