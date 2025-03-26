package com.optimed.repository;


import com.optimed.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByDoctorId(UUID doctorId);
}
