package com.optimed.web;

import com.optimed.TestSecurityConfig;
import com.optimed.entity.*;
import com.optimed.service.MedicalRecordService;
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

@WebMvcTest(MedicalRecordController.class)
@Import(TestSecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    private PatientProfile patient;
    private MedicalRecord medicalRecord;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        patient = PatientProfile.builder()
                .id(UUID.randomUUID())
                .fullName("John")
                .build();

        medicalRecord = MedicalRecord.builder()
                .id(UUID.randomUUID())
                .patient(patient)
                .diagnosis("Flu")
                .treatmentPlan("Rest and hydration")
                .recordDate(LocalDate.now())
                .notes("Follow-up in one week")
                .build();
    }


    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    void shouldCreateMedicalRecord() throws Exception {
        Mockito.doNothing().when(medicalRecordService).createMedicalRecord(any());

        mockMvc.perform(post("/doctor/medical-record")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("patientId", patientId.toString())
                        .param("description", "Test medical record"))
                .andExpect(status().isOk())
                .andExpect(content().string("Medical record saved successfully!"));
    }

    @Test
    @WithMockUser(username = "doctorUser", roles = {"DOCTOR"})
    void shouldReturnMedicalRecordsForPatient() throws Exception {
        when(medicalRecordService.getRecordsForPatient(patientId)).thenReturn(List.of(medicalRecord));

        mockMvc.perform(get("/doctor/medical-records/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].diagnosis").value("Flu"))
                .andExpect(jsonPath("$[0].treatmentPlan").value("Rest and hydration"))
                .andExpect(jsonPath("$[0].recordDate").value("2025-04-01"))
                .andExpect(jsonPath("$[0].notes").value("Follow-up in one week"));
    }
}
