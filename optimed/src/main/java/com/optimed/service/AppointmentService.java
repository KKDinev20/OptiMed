package com.optimed.service;

import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.Appointment;
import com.optimed.entity.DoctorProfile;
import com.optimed.entity.PatientProfile;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public long countAppointments() {
        return appointmentRepository.count();
    }

    public Page<Appointment> getAllAppointments(String filter, Pageable pageable) {
        if (filter != null && !filter.isEmpty()) {
            return appointmentRepository.findByDoctorNameContainingOrPatientNameContainingOrStatus(
                    filter, filter, AppointmentStatus.valueOf(filter), pageable);
        }
        return appointmentRepository.findAll(pageable);
    }

    public void createAppointment(UUID patientId, AppointmentRequest request) {
        DoctorProfile doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

        PatientProfile patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .status(AppointmentStatus.BOOKED)
                .build();

        appointmentRepository.save(appointment);
    }

    public void approveAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    public void cancelAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }
}
