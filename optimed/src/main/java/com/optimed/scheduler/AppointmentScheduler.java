package com.optimed.scheduler;

import com.optimed.dto.CancelAppointmentRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void autoCancelExpiredAppointments() {
        LocalTime threshold = LocalTime.now().minusHours (24);
        List<Appointment> expiredAppointments = appointmentRepository
                .findByStatusAndAppointmentTimeBefore(AppointmentStatus.BOOKED, threshold);

        for (Appointment appointment : expiredAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELED);
            appointmentRepository.save(appointment);
        }

        CancelAppointmentRequest logEntry = new CancelAppointmentRequest (expiredAppointments.size(), LocalTime.now());
        System.out.println("✅ Auto-canceled " + logEntry.getCanceledAppointments () + " expired appointments at " + logEntry.getExecutedAt());
    }
}
