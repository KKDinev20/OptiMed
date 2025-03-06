package com.optimed.dto;


import lombok.Data;

@Data
public class DashboardStats {
    private long totalUsers;
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long pendingAppointments;
    private long completedAppointments;
}
