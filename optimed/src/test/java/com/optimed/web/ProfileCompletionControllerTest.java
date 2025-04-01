package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.dto.DoctorRequest;
import com.optimed.dto.PatientRequest;
import com.optimed.entity.User;
import com.optimed.entity.enums.Role;
import com.optimed.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@WebMvcTest(ProfileCompletionController.class)
@ExtendWith(SpringExtension.class)
@Import(TestSecurityConfig.class)
class ProfileCompletionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User doctorUser;
    private User patientUser;

    @BeforeEach
    void setUp() {
        doctorUser = new User();
        doctorUser.setUsername("doctorUser");
        doctorUser.setRole(Role.DOCTOR);

        patientUser = new User();
        patientUser.setUsername("patientUser");
        patientUser.setRole(Role.PATIENT);
    }

    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    void shouldShowDoctorProfileForm() throws Exception {
        when(userService.findByUsername("doctorUser")).thenReturn(Optional.of(doctorUser));

        mockMvc.perform(get("/complete-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/complete-profile"))
                .andExpect(model().attributeExists("doctorRequest"));
    }

    @Test
    @WithMockUser(username = "patientUser", roles = {"PATIENT"})
    void shouldShowPatientProfileForm() throws Exception {
        when(userService.findByUsername("patientUser")).thenReturn(Optional.of(patientUser));

        mockMvc.perform(get("/complete-profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/complete-profile"))
                .andExpect(model().attributeExists("patientRequest"));
    }

    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    void shouldCompleteDoctorProfile() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userService.storeImage(any(MultipartFile.class))).thenReturn("/images/uploaded.png");

        doNothing().when(userService).completeDoctorProfile(anyString(), any(DoctorRequest.class));

        mockMvc.perform(multipart("/complete-profile/doctor")
                        .file("avatarFile", "fake-image-data".getBytes())
                        .param("name", "Dr. John Doe")
                        .param("specialty", "Cardiology"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/dashboard"));

        verify(userService, times(1)).completeDoctorProfile(eq("doctorUser"), any(DoctorRequest.class));
    }

    @Test
    @WithMockUser(username = "patientUser", roles = {"PATIENT"})
    void shouldCompletePatientProfile() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(userService.storeImage(any(MultipartFile.class))).thenReturn("/images/uploaded.png");

        doNothing().when(userService).completePatientProfile(anyString(), any(PatientRequest.class));

        mockMvc.perform(multipart("/complete-profile/patient")
                        .file("avatarFile", "fake-image-data".getBytes())
                        .param("name", "John Doe")
                        .param("age", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/dashboard"));

        verify(userService, times(1)).completePatientProfile(eq("patientUser"), any(PatientRequest.class));
    }
}
