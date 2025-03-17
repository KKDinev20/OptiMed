package com.optimed.service;

import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.repository.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.*;
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

    public Page<Appointment> getAllAppointments (Pageable pageable) {
        return appointmentRepository.findAll (pageable);
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
            boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                    request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime());

            if (exists) {
                throw new RuntimeException("This doctor is already booked at the selected date and time.");
            }

            Appointment appointment = Appointment.builder()
                    .doctor(doctor.get())
                    .patient(patient.get())
                    .appointmentDate(request.getAppointmentDate())
                    .appointmentTime(request.getAppointmentTime())
                    .reason(request.getReason())
                    .status(AppointmentStatus.BOOKED)
                    .build();

            appointmentRepository.save(appointment);
        } else {
            throw new RuntimeException("Doctor or Patient not found");
        }
    }


    public long getCanceledAppointments() {
        return appointmentRepository.countByStatus (AppointmentStatus.CANCELED);
    }

    public long getBookedAppointments() {
        return appointmentRepository.countByStatus (AppointmentStatus.BOOKED);
    }

    public long getTodaysAppointments() {
        return appointmentRepository.countByAppointmentDate(
                LocalDate.from(Instant.now().atZone(ZoneId.systemDefault()))
        );
    }

    public void cancelAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
    }

    public void rescheduleAppointment(UUID appointmentId, LocalDate newDate, LocalTime newTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointmentRepository.save(appointment);
    }


    public Page<Appointment> getUpcomingAppointmentsForMonth(Pageable pageable) {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        return appointmentRepository.findByAppointmentDateBetween (firstDayOfMonth, lastDayOfMonth, pageable);
    }

    public Page<Appointment> searchAppointments(UUID doctorId, AppointmentStatus status, String patientName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return appointmentRepository.searchAppointments(doctorId, status, patientName, startDate, endDate, pageable);
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

    public List<PatientProfile> getPatientsByDoctor(UUID doctorId) {
        return appointmentRepository.findPatientsByDoctor(doctorId);
    }


    public Appointment getAppointmentById (UUID appointmentId) {
        return appointmentRepository.findById(appointmentId).orElseThrow();
    }
}
