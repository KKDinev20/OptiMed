package com.optimed.service;

import com.optimed.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;


    public long countAppointments() {
        return appointmentRepository.count();
    }
}
