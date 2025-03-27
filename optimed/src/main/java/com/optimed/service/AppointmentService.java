package com.optimed.service;

import com.optimed.client.NotificationClient;
import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.repository.*;
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
    private final NotificationClient notificationClient;

    public long countAppointments () {
        return appointmentRepository.count ();
    }

    public Page<Appointment> getAppointmentsByPatientId(UUID patientId, Pageable pageable,
                                                        String doctorName,
                                                        AppointmentStatus status,
                                                        LocalDate startDate,
                                                        LocalDate endDate) {

        return appointmentRepository.findAppointmentsByFilters(patientId, doctorName, status, startDate, endDate, pageable);
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

    public boolean isAppointmentSlotAvailable(UUID doctorId, LocalDate date, LocalTime time) {
        return appointmentRepository.countByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                doctorId, date, time, AppointmentStatus.CONFIRMED) == 0;
    }



    public void createAppointment(AppointmentRequest request) {
        Optional<DoctorProfile> doctor = doctorRepository.findById(request.getDoctorId());
        Optional<PatientProfile> patient = patientRepository.findById(request.getPatientId());

        if (!isAppointmentSlotAvailable(request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime())) {
            throw new RuntimeException("The doctor already has an appointment at this time.");
        }

        if (doctor.isEmpty() || patient.isEmpty()) {
            throw new RuntimeException("Doctor or Patient not found.");
        }

        DoctorProfile doctorProfile = doctor.get();

        boolean slotExists = doctorProfile.getAvailableTimeSlots().stream()
                .anyMatch(slot -> !request.getAppointmentTime().isBefore(slot.getStartTime()) &&
                        !request.getAppointmentTime().isAfter(slot.getEndTime()));


        if (!slotExists) {
            throw new RuntimeException("The selected time slot is not available for this doctor.");
        }

        boolean doctorBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime());

        if (doctorBooked) {
            throw new RuntimeException("This doctor is already booked at the selected date and time.");
        }

        boolean patientBooked = appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(
                request.getPatientId(), request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime());

        if (patientBooked) {
            throw new RuntimeException("You have already booked an appointment with this doctor at the selected date and time.");
        }

        LocalDateTime cancellationDeadline = LocalDateTime.of(request.getAppointmentDate(), request.getAppointmentTime()).minusHours(24);

        Appointment appointment = Appointment.builder()
                .doctor(doctor.get())
                .patient(patient.get())
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .cancellationDeadline(cancellationDeadline)
                .reason(request.getReason())
                .status(AppointmentStatus.BOOKED)
                .build();

        appointmentRepository.save(appointment);
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

        notificationClient.sendNotification(appointment.getPatient().getEmail(),
                "Your appointment on " + appointment.getAppointmentDate() + " has been canceled.");
    }


    public void rescheduleAppointment(UUID appointmentId, LocalDate newDate, LocalTime newTime) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointmentRepository.save(appointment);
    }

    public void approveAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NoSuchElementException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException("Only pending appointments can be approved.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
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
        return appointmentRepository.findTop3ByOrderByIdDesc();
    }

    public void updateAppointmentStatus(UUID appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(status);
        appointmentRepository.save(appointment);
    }

    public List<PatientProfile> getPatientsByDoctor(UUID doctorId) {
        return appointmentRepository.findPatientsByDoctor(doctorId);
    }

    public boolean isDoctorAvailable(UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        List<Appointment> existingAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndAppointmentTime(doctorId, appointmentDate, appointmentTime);
        return existingAppointments.isEmpty();
    }

    public Appointment getAppointmentById (UUID appointmentId) {
        return appointmentRepository.findById(appointmentId).orElseThrow();
    }

    public void deleteAppointment(UUID appointmentId) {
        appointmentRepository.deleteById (appointmentId);
    }
}
