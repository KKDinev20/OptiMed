package com.optimed.repository;

import com.optimed.entity.DoctorProfile;
import com.optimed.entity.enums.Specialization;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {

    List<DoctorProfile> findBySpecialization(Specialization specialization);

    @Query("SELECT d FROM DoctorProfile d WHERE SIZE(d.reviews) >= :minReviews")
    List<DoctorProfile> findByReviewsGreaterThanEqual(@Param("minReviews") Integer minReviews);

    @Query("SELECT d FROM DoctorProfile d WHERE d.specialization = :specialization AND SIZE(d.reviews) >= :minReviews")
    List<DoctorProfile> findBySpecializationAndReviewsGreaterThanEqual(@Param("specialization") Specialization specialization, @Param("minReviews") Integer minReviews);
}
