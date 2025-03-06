package com.optimed.repository;

import com.optimed.entity.Appointment;
import com.optimed.entity.enums.AppointmentStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a " +
            "WHERE LOWER(a.doctor.fullName) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR a.status = :status")
    Page<Appointment> findByDoctorNameContainingOrPatientNameContainingOrStatus(
            String doctorName, String patientName, AppointmentStatus status, Pageable pageable);

    List<Appointment> findByDoctorId (UUID doctorId);

    @Query("SELECT a FROM Appointment a " +
    "WHERE a.status = 'PENDING'")
    long countByStatus (String status);
}