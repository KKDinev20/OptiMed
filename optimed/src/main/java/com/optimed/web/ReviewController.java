package com.optimed.web;

import com.optimed.dto.ReviewRequest;
import com.optimed.entity.Review;
import com.optimed.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/reviews")
@PreAuthorize("hasRole('ROLE_PATIENT')")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<String> leaveReview(@Valid @ModelAttribute ReviewRequest request) {
        reviewService.leaveReview(request);
        return ResponseEntity.ok("Review submitted successfully!");
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<List<Review>> getDoctorReviews(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(reviewService.getDoctorReviews(doctorId));
    }
}
