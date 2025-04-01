package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.entity.*;
import com.optimed.service.ReviewService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    private UUID doctorId;
    private UUID patientId;
    private Review review;
    private DoctorProfile doctor;
    private PatientProfile patient;

    @BeforeEach
    void setUp() {
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctor = DoctorProfile.builder()
                .id(doctorId)
                .fullName("Dr. Smith")
                .build();

        patient = PatientProfile.builder()
                .id(patientId)
                .fullName ("Alice")
                .build();

        review = Review.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .patient(patient)
                .rating(5)
                .comment("Excellent service and very knowledgeable doctor.")
                .build();
    }


    @Test
    @WithMockUser(username = "patientUser", roles = {"PATIENT"})
    void shouldSubmitReview() throws Exception {
        Mockito.doNothing().when(reviewService).leaveReview(any());

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("doctorId", doctorId.toString())
                        .param ("patientId", patientId.toString())
                        .param("comment", "Excellent doctor!")
                        .param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Review submitted successfully!"));
    }

    @Test
    @WithMockUser(username = "patientUser", roles = {"PATIENT"})
    void shouldReturnDoctorReviews() throws Exception {
        when(reviewService.getDoctorReviews(doctorId)).thenReturn(List.of(review));

        mockMvc.perform(get("/reviews/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].comment").value("Excellent service and very knowledgeable doctor."))
                .andExpect(jsonPath("$[0].rating").value(5));
    }
}
