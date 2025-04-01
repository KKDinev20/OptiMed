package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.entity.*;
import com.optimed.service.PrescriptionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrescriptionController.class)
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PrescriptionService prescriptionService;

    private UUID doctorId;
    private UUID patientId;
    private DoctorProfile doctor;
    private PatientProfile patient;
    private Prescription prescription;

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

        prescription = Prescription.builder()
                .id(UUID.randomUUID())
                .doctor(doctor)
                .patient(patient)
                .medicationDetails("Paracetamol")
                .dosageInstructions("Take 1 tablet every 8 hours")
                .dateIssued(LocalDate.now())
                .build();
    }

    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    void shouldCreatePrescription() throws Exception {
        Mockito.doNothing().when(prescriptionService).createPrescription(any());

        mockMvc.perform(post("/doctor/prescription")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("patientId", patientId.toString())
                        .param("doctorId", doctorId.toString())
                        .param("medicationDetails", "Paracetamol")
                        .param("dosageInstructions", "Take 1 tablet every 8 hours"))
                .andExpect(status().isOk())
                .andExpect(content().string("Prescription saved successfully!"));
    }

    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    void shouldReturnPrescriptionsForPatient() throws Exception {
        when(prescriptionService.getPrescriptionsForPatient(patientId)).thenReturn(List.of(prescription));

        mockMvc.perform(get("/doctor/prescriptions/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].medicationDetails").value("Paracetamol"))
                .andExpect(jsonPath("$[0].dosageInstructions").value("Take 1 tablet every 8 hours"));
    }
}
