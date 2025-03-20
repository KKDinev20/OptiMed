package com.optimed.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardStats {
    private long totalUsers;
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long pendingAppointments;
    private long bookedAppointments;
    private long confirmedAppointments;
    private Map<String, Long> appointmentsByStatus;
}
