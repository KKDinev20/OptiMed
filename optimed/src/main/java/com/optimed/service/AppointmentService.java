package com.optimed.service;

import com.optimed.entity.Appointment;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;


    public long countAppointments() {
        return appointmentRepository.count();
    }

    public Page<Appointment> getAllAppointments(String filter, Pageable pageable) {
        if (filter != null && !filter.isEmpty()) {
            return appointmentRepository.findByDoctorNameContainingOrPatientNameContainingOrStatus(
                    filter, AppointmentStatus.valueOf(filter), pageable);
        }
        return appointmentRepository.findAll(pageable);
    }
}
