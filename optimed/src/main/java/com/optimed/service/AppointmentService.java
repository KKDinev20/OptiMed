package com.optimed.service;

import com.optimed.entity.Appointment;
import com.optimed.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;


    public long countAppointments() {
        return appointmentRepository.count();
    }

    public Page<Appointment> getAllAppointments (Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }
}
