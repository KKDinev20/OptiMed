package com.optimed.web;

import com.optimed.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@Controller
@RequiredArgsConstructor
public class ReportsController {

    private final ReportService reportService;

    @GetMapping("/admin/reports")
    public String reportsPage(Model model) {
        model.addAttribute("appointmentsPerDay", reportService.getAppointmentsOverTime ());
        model.addAttribute("userRegistrations", reportService.getUserRegistrations());
        return "admin/reports";
    }

    @GetMapping("/admin/reports/download")
    public ResponseEntity<InputStreamResource> downloadReport() {
        ByteArrayInputStream in = reportService.generateCsvReport();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=system_report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/admin/reports/export/csv")
    public ResponseEntity<InputStreamResource> downloadCSV() {
        ByteArrayInputStream data = reportService.generateCsvReport ();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(data));
    }

}
