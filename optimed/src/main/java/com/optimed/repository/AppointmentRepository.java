package com.optimed.repository;

import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") AppointmentStatus status);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.status = :status " +
            "AND FUNCTION('TIMESTAMP', a.appointmentDate, a.appointmentTime) < :threshold")
    List<Appointment> findExpiredAppointments(
            @Param("status") AppointmentStatus status,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.appointmentTime = :time AND a.status = :status")
    int countByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(UUID doctorId, LocalDate date, LocalTime time, AppointmentStatus status);


    List<Appointment> findTop3ByOrderByIdDesc();
    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);

    long countByAppointmentDate (LocalDate appointmentDate);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:patientName IS NULL OR LOWER(a.patient.fullName) LIKE LOWER(CONCAT('%', :patientName, '%'))) " +
            "AND (:startDate IS NULL OR a.appointmentDate >= :startDate) " +
            "AND (:endDate IS NULL OR a.appointmentDate <= :endDate)")
    Page<Appointment> searchAppointments(
            @Param("doctorId") UUID doctorId,
            @Param("status") AppointmentStatus status,
            @Param("patientName") String patientName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
            "AND (:doctorName IS NULL OR a.doctor.fullName LIKE %:doctorName%) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:startDate IS NULL OR a.appointmentDate >= :startDate) " +
            "AND (:endDate IS NULL OR a.appointmentDate <= :endDate)")
    Page<Appointment> findAppointmentsByFilters(UUID patientId,
                                                @Param("doctorName") String doctorName,
                                                @Param("status") AppointmentStatus status,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                Pageable pageable);

    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<PatientProfile> findPatientsByDoctor(@Param("doctorId") UUID doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDateAndAppointmentTime(UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime);

    boolean existsByPatientIdAndDoctorIdAndAppointmentDateAndAppointmentTime(UUID patientId, UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime);
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(UUID doctorId, LocalDate appointmentDate, LocalTime appointmentTime);
    Page<Appointment> findByAppointmentDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Appointment> findByAppointmentDate (LocalDate localDate);
}