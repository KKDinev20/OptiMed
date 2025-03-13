package com.optimed.repository;

import com.optimed.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorProfile, UUID> {
    Optional<DoctorProfile> findByUserId(UUID userId);
    Optional<DoctorProfile> findByUserUsername(String username);
}