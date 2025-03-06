package com.optimed.repository;

import com.optimed.entity.DoctorProfile;
import com.optimed.entity.enums.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    List<DoctorProfile> findBySpecialization(Specialization specialization);
}
