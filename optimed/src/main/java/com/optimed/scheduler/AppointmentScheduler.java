package com.optimed.scheduler;

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
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<Appointment> expiredAppointments = appointmentRepository.findExpiredAppointments(AppointmentStatus.BOOKED, threshold);

        for (Appointment appointment : expiredAppointments) {
            appointment.setStatus(AppointmentStatus.CANCELED);
            appointmentRepository.save(appointment);
        }

        System.out.println("✅ Auto-canceled " + expiredAppointments.size() + " expired appointments at " + LocalDateTime.now());
    }
}
