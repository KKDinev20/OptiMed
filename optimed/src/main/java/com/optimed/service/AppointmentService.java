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

import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public long countAppointments () {
        return appointmentRepository.count ();
    }

    public Page<Appointment> getAppointmentsByPatientId(UUID patientId, Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable);
    }


    public long countAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.countByStatus(status);
    }

    public Map<String, Long> countAppointmentsByStatus() {
        return Map.of(
                "Pending", countAppointmentsByStatus(AppointmentStatus.CANCELED),
                "Booked", countAppointmentsByStatus(AppointmentStatus.BOOKED),
                "Confirmed", countAppointmentsByStatus(AppointmentStatus.CONFIRMED)
        );
    }


    public void createAppointment(AppointmentRequest request) {
        Optional<DoctorProfile> doctor = doctorRepository.findById(request.getDoctorId());
        Optional<PatientProfile> patient = patientRepository.findById(request.getPatientId());

        if (doctor.isPresent() && patient.isPresent()) {
            Appointment appointment = Appointment.builder()
                    .doctor(doctor.get())
                    .patient(patient.get())
                    .appointmentDate(request.getAppointmentDate())
                    .appointmentTime (request.getAppointmentTime ())
                    .specialization(request.getSpecialization())
                    .reason(request.getReason())
                    .status(AppointmentStatus.BOOKED)
                    .build();

            appointmentRepository.save (appointment);
            return;
        }

        throw new RuntimeException("Doctor or Patient not found");
    }


    public void cancelAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(EntityNotFoundException::new);
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save (appointment);
    }

    public List<Appointment> getRecentAppointments () {
        return appointmentRepository.findTop10ByOrderByIdDesc();
    }

    public Page<Appointment> getAppointmentsByDoctorId(UUID doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }

    public void updateAppointmentStatus(UUID appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }

}
