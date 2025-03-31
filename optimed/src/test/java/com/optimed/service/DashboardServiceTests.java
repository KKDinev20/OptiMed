package com.optimed.service;

import com.optimed.dto.DashboardStats;
import com.optimed.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private DashboardService dashboardService;

    private DashboardStats expectedStats;

    @BeforeEach
    void setUp() {
        expectedStats = new DashboardStats();

        when(userRepository.count()).thenReturn(100L);
        when(doctorRepository.count()).thenReturn(20L);
        when(patientRepository.count()).thenReturn(80L);
        when(appointmentRepository.count()).thenReturn(150L);

        Map<String, Long> statusCounts = Map.of(
                "Pending", 30L,
                "Booked", 40L,
                "Confirmed", 80L
        );
        when(appointmentService.countAppointmentsByStatus()).thenReturn(statusCounts);
        when(appointmentService.countAppointments()).thenReturn(150L);
    }

    @Test
    void shouldGetDashboardStats() {
        // When
        DashboardStats actualStats = dashboardService.getDashboardStats();

        // Then
        assertEquals(100L, actualStats.getTotalUsers());
        assertEquals(20L, actualStats.getTotalDoctors());
        assertEquals(80L, actualStats.getTotalPatients());
        assertEquals(150L, actualStats.getTotalAppointments());

        assertEquals(30L, actualStats.getPendingAppointments());
        assertEquals(40L, actualStats.getBookedAppointments());
        assertEquals(80L, actualStats.getConfirmedAppointments());

        verify(userRepository).count();
        verify(doctorRepository).count();
        verify(patientRepository).count();
        verify(appointmentService).countAppointments();
        verify(appointmentService).countAppointmentsByStatus();
    }

    @Test
    void shouldGetDashboardStatsWhenAllCountsAreZero() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(doctorRepository.count()).thenReturn(0L);
        when(patientRepository.count()).thenReturn(0L);
        when(appointmentRepository.count()).thenReturn(0L);
        when(appointmentService.countAppointments()).thenReturn(0L);
        when(appointmentService.countAppointmentsByStatus())
                .thenReturn(Map.of("Pending", 0L, "Booked", 0L, "Confirmed", 0L));

        // When
        DashboardStats actualStats = dashboardService.getDashboardStats();

        // Then
        assertEquals(0L, actualStats.getTotalUsers());
        assertEquals(0L, actualStats.getTotalDoctors());
        assertEquals(0L, actualStats.getTotalPatients());
        assertEquals(0L, actualStats.getTotalAppointments());
        assertEquals(0L, actualStats.getPendingAppointments());
        assertEquals(0L, actualStats.getBookedAppointments());
        assertEquals(0L, actualStats.getConfirmedAppointments());
    }

    @Test
    void shouldGetDashboardStatsWhenAppointmentServiceReturnsEmptyMap() {
        // Given
        when(appointmentService.countAppointmentsByStatus())
                .thenReturn(Collections.emptyMap());

        // When
        DashboardStats actualStats = dashboardService.getDashboardStats();

        // Then
        assertEquals(100L, actualStats.getTotalUsers());
        assertEquals(20L, actualStats.getTotalDoctors());
        assertEquals(80L, actualStats.getTotalPatients());
        assertEquals(150L, actualStats.getTotalAppointments());

        assertEquals(0L, actualStats.getPendingAppointments());
        assertEquals(0L, actualStats.getBookedAppointments());
        assertEquals(0L, actualStats.getConfirmedAppointments());
    }
}