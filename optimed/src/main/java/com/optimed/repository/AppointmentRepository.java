package com.optimed.repository;

import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a " +
            "WHERE LOWER(a.doctor.fullName) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :filter, '%')) " +
            "OR a.status = :status")
    Page<Appointment> findByDoctorNameContainingOrPatientNameContainingOrStatus(
            String doctorName, String patientName, AppointmentStatus status, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") AppointmentStatus status);

    List<Appointment> findTop10ByOrderByIdDesc();
    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);

    long countByAppointmentDate (LocalDate appointmentDate);

    Page<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);}