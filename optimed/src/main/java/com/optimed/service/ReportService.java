package com.optimed.service;

import com.optimed.repository.AppointmentRepository;
import com.optimed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern ("yyyy-MM-dd");

    public Map<String, Long> getUserRegistrations () {
        return userRepository.findAll ().stream ()
                .filter (user -> user.getCreatedAt () != null)
                .collect (Collectors.groupingBy (user -> user.getCreatedAt ().toLocalDate ().format (DATE_FORMATTER), Collectors.counting ()));
    }

    public Map<String, Long> getAppointmentsOverTime () {
        return appointmentRepository.findAll ().stream ()
                .collect (Collectors.groupingBy (appointment -> appointment.getAppointmentDate ().toLocalDate ().format (DATE_FORMATTER), Collectors.counting ()));
    }

    public ByteArrayInputStream generateCsvReport () {
        List<String[]> data = List.of (
                new String[]{"Date", "Appointments", "New Users"},
                new String[]{"2025-02-01", "10", "5"},
                new String[]{"2025-02-02", "15", "7"}
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream ();
        PrintWriter writer = new PrintWriter (out);
        data.forEach (row -> writer.println (String.join (",", row)));
        writer.flush ();

        return new ByteArrayInputStream (out.toByteArray ());
    }
}
