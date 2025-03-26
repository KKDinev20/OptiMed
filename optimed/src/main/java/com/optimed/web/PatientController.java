package com.optimed.web;

import com.optimed.dto.AppointmentRequest;
import com.optimed.dto.EditPatientRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.entity.enums.Specialization;
import com.optimed.mapper.PatientMapper;
import com.optimed.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/patient")
@PreAuthorize("hasRole('ROLE_PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final DoctorProfileService doctorProfileService;
    private final UserService userService;
    private final MedicalRecordService medicalRecordService;
    private final ReviewService reviewService;
    private final PrescriptionService prescriptionService;
    private final PatientService patientService;

    @GetMapping("/appointments/new")
    public String showAppointmentForm (Model model) {
        model.addAttribute ("appointmentRequest", new AppointmentRequest ());
        model.addAttribute ("currentUserPage", "Add Appointment");
        model.addAttribute ("specializations", Specialization.values ());
        return "patient/appointments/create";
    }

    @PostMapping("/appointments")
    public String addAppointment (@ModelAttribute AppointmentRequest request,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            Optional<PatientProfile> patient = patientService.getPatientByUsername (authentication.getName ());

            if (patient.isEmpty ()) {
                model.addAttribute ("error", "Patient not found.");
                model.addAttribute ("specializations", Specialization.values ());
                return "patient/appointments/create";
            }

            boolean isAvailable = appointmentService.isDoctorAvailable (request.getDoctorId (),
                    request.getAppointmentDate (),
                    request.getAppointmentTime ());
            if (!isAvailable) {
                model.addAttribute ("error", "Doctor is not available at the selected time.");
                model.addAttribute ("specializations", Specialization.values ());
                if (request.getSpecialization () != null) {
                    model.addAttribute ("doctors",
                            doctorProfileService.findDoctorsBySpecialization (request.getSpecialization ()));
                }
                return "patient/appointments/create";
            }

            request.setPatientId (patient.get ().getId ());
            appointmentService.createAppointment (request);

            redirectAttributes.addFlashAttribute ("success", "Appointment created successfully.");
            return "redirect:/patient/appointments";
        } catch (IllegalArgumentException e) {
            model.addAttribute ("error", "Invalid doctor ID format. Please select a doctor from the dropdown.");
            model.addAttribute ("specializations", Specialization.values ());
            return "patient/appointments/create";
        } catch (Exception e) {
            model.addAttribute ("error", "An error occurred: " + e.getMessage ());
            model.addAttribute ("specializations", Specialization.values ());
            return "patient/appointments/create";
        }
    }


    @GetMapping("/dashboard")
    public String dashboard (Model model, Principal principal) {
        User user = userService.findByUsername (principal.getName ()).orElseThrow ();
        PatientProfile patient = patientService.findByUser (user).orElseThrow ();

        UUID patientId = patient.getId ();
        model.addAttribute ("currentUserPage", "Dashboard");
        model.addAttribute ("patientId", patientId);

        return "patient/dashboard";
    }

    @GetMapping("/doctors/{specialization}")
    @ResponseBody
    public List<DoctorProfile> getDoctorsBySpecialization (
            @PathVariable Specialization specialization) {

        return doctorProfileService.findDoctorsBySpecialization (specialization);
    }


    @GetMapping("/appointments")
    public String getPatientAppointments (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model,
            Principal principal) {

        User user = userService.findByUsername(principal.getName()).orElseThrow();
        PatientProfile patient = patientService.findByUser(user).orElseThrow();
        UUID patientId = patient.getId();

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId, pageable, doctorName, status, startDate, endDate);

        model.addAttribute("appointments", appointments);
        model.addAttribute("patientId", patientId);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentUserPage", "My Appointments");
        model.addAttribute("totalPages", appointments.getTotalPages());
        model.addAttribute("size", size);

        return "patient/appointments/list-appointments";
    }


    @PostMapping("/cancel-appointment/{appointmentId}")
    public String cancelAppointment(@PathVariable UUID appointmentId, RedirectAttributes redirectAttrs) {
        appointmentService.cancelAppointment(appointmentId);
        redirectAttrs.addFlashAttribute("success", "Appointment canceled successfully.");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/appointments/{appointmentId}/reschedule")
    public String showRescheduleForm(@PathVariable("appointmentId") UUID appointmentId, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);

        model.addAttribute ("currentUserPage", "Reschedule");
        model.addAttribute("appointment", appointment);
        return "patient/appointments/reschedule-appointment";
    }

    @GetMapping("/settings")
    public String getPatientSettings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        PatientProfile patientProfile = patientService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        EditPatientRequest editPatientRequest = PatientMapper.mapToEditPatient (patientProfile);

        model.addAttribute("patient", editPatientRequest);
        model.addAttribute ("currentUserPage", "Settings");

        model.addAttribute("specializations", Specialization.values());

        return "patient/settings";
    }

    @PostMapping("/settings")
    public String completePatientProfile(
            @ModelAttribute EditPatientRequest editPatientRequest,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String imageUrl = userService.storeImage(avatarFile);
            editPatientRequest.setAvatarUrl(imageUrl);
        } else {
            editPatientRequest.setAvatarUrl("/dashboard/img/default.png");
        }

        patientService.updatePatientProfile(userDetails.getUsername(), editPatientRequest);
        return "redirect:/patient/dashboard";
    }

    @GetMapping("/doctor")
    public String listAvailableDoctors(
            @RequestParam(required = false) Specialization specialization,
            @RequestParam(required = false) Integer minReviews,
            Model model) {

        List<DoctorProfile> doctors = doctorProfileService.findDoctors(specialization, minReviews);

        model.addAttribute("doctors", doctors);
        model.addAttribute("specializations", Specialization.values());
        model.addAttribute("currentUserPage", "Available Doctors");

        return "patient/doctor/doctor-list";
    }


    @GetMapping("/doctor/{doctorId}")
    public String viewDoctorDetails(@PathVariable UUID doctorId, Model model, Principal principal) {
        DoctorProfile doctor = doctorProfileService.getDoctorById(doctorId);
        if (doctor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        User user = userService.findByUsername (principal.getName ()).orElseThrow ();
        PatientProfile patient = patientService.findByUser (user).orElseThrow ();        List<Review> reviews = reviewService.getDoctorReviews(doctorId);
        model.addAttribute("doctor", doctor);
        model.addAttribute("reviews", reviews);
        model.addAttribute("patient", patient);
        return "patient/doctor/doctor-details";
    }

    @PostMapping("/appointments/{appointmentId}/reschedule")
    public String rescheduleAppointment(@PathVariable UUID appointmentId,
                                        @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
                                        @RequestParam("newTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime) {
        appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);
        return "redirect:/patient/appointments";
    }

    @GetMapping("/appointments/{appointmentId}")
    public String getAppointmentDetails(@PathVariable UUID appointmentId, Model model) {
        model.addAttribute("currentUserPage", "Appointment details");

        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            model.addAttribute("appointment", appointment);
            return "doctor/appointments/appointment-details";
        } catch (NoSuchElementException e) {
            model.addAttribute("error", "Appointment not found.");
            model.addAttribute("errorCode", 404);
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred.");
            model.addAttribute("errorCode", 500);
            return "error/error";
        }
    }

    @GetMapping("/medical-history")
    public String listMedicalHistory(Model model,  Principal principal) {
        User user = userService.findByUsername (principal.getName ()).orElseThrow ();
        PatientProfile patient = patientService.findByUser (user).orElseThrow ();

        UUID patientId = patient.getId ();

        List<Prescription> prescriptions = prescriptionService.getPrescriptionsForPatient(patientId);
        List<MedicalRecord> records = medicalRecordService.getRecordsForPatient(patientId);

        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("medicalRecords", records);

        return "patient/medical-history";
    }

}
