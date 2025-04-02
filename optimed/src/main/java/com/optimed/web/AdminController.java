package com.optimed.web;

import com.optimed.dto.*;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.service.*;
import com.optimed.client.NotificationClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final DoctorService doctorService;
    private final NotificationClient notificationClient;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DashboardService dashboardService;


    @GetMapping("/dashboard")
    public ModelAndView dashboard(Model model) {
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalDoctors", doctorService.countDoctors());
        model.addAttribute("totalPatients", patientService.countPatients());
        model.addAttribute("recentUsers", userService.getRecentUsers());
        model.addAttribute("recentAppointments", appointmentService.getRecentAppointments());
        model.addAttribute("totalAppointments", appointmentService.countAppointments());
        model.addAttribute("currentPage", "Dashboard");

        return new ModelAndView("admin/dashboard");
    }

    @GetMapping("/stats")
    @ResponseBody
    public DashboardStats getDashboardStats() {
        DashboardStats stats = dashboardService.getDashboardStats();

        Map<String, Long> statusStats = appointmentService.countAppointmentsByStatus();

        stats.setPendingAppointments(statusStats.getOrDefault("Pending", 0L));
        stats.setBookedAppointments(statusStats.getOrDefault("Booked", 0L));
        stats.setConfirmedAppointments(statusStats.getOrDefault("Confirmed", 0L));

        stats.setAppointmentsByStatus(statusStats);

        return stats;
    }

    @PostMapping("/delete-appointment/{appointmentId}")
    public String deleteAppointment (@PathVariable UUID appointmentId) {
        appointmentService.deleteAppointment (appointmentId);
        return "redirect:/admin/manage-appointments";
    }




    @GetMapping("/manage-users")
    public ModelAndView manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<User> users = userService.getAllNonAdminUsers(PageRequest.of(page, size));

        model.addAttribute("users", users);
        model.addAttribute("currentPage", "Manage Users");
        model.addAttribute("pageSize", size);

        return new ModelAndView("admin/manage-users");
    }

    @GetMapping("/manage-appointments")
    public ModelAndView manageAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Appointment> appointments = appointmentService.getAllAppointments(PageRequest.of(page, size));

        model.addAttribute("appointments", appointments);
        model.addAttribute("currentPage", "Manage Appointments");
        model.addAttribute("pageSize", size);
        model.addAttribute("pageSizes", Arrays.asList(5, 10, 15, 20));

        return new ModelAndView("admin/manage-appointments");
    }

    @GetMapping("/edit-user/{userId}")
    public ModelAndView editUser (@PathVariable UUID userId, Model model) {
        User user = userService.getUserById (userId);
        model.addAttribute ("currentPage", "Edit User");
        model.addAttribute ("user", user);
        return new ModelAndView ("admin/edit-user");
    }

    @PostMapping("/edit-user/{userId}")
    public String updateUser (
            @PathVariable UUID userId,
            @Valid @ModelAttribute("user") UserRequest userRequest,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "admin/edit-user";
        }

        userService.updateUser (userId, userRequest);
        return "redirect:/admin/manage-users?updated=true";
    }


    @PostMapping("/delete-user/{userId}")
    public String deleteUser (@PathVariable UUID userId) {
        userService.deleteUser (userId);
        return "redirect:/admin/manage-users";
    }


    @GetMapping("/settings")
    public String getSettingsPage (@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername ();
        User user = userService.findByUsername (username)
                .orElseThrow (() -> new RuntimeException ("User not found"));

        if (user == null) {
            model.addAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        UserRequest userRequest = new UserRequest ();
        userRequest.setUsername (user.getUsername ());
        userRequest.setEmail (user.getEmail ());
        userRequest.setRole (user.getRole ());
        model.addAttribute ("userRequest", userRequest);
        model.addAttribute ("currentPage", "Settings");

        return "admin/settings";
    }


    @PostMapping("/settings")
    public String updateUser (@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("userRequest") UserRequest userRequest,
                              BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors ()) {
            model.addAttribute ("errors", bindingResult.getAllErrors ());
            return "admin/settings";
        }

        String username = userDetails.getUsername ();
        User user = userService.findByUsername (username)
                .orElseThrow (() -> new RuntimeException ("User not found"));

        userService.updateUser (user.getId (), userRequest);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/appointments/{appointmentId}/reschedule")
    public String showRescheduleForm(@PathVariable("appointmentId") UUID appointmentId, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        model.addAttribute ("currentUserPage", "Reschedule Appointment");
        model.addAttribute("appointment", appointment);
        return "admin/manage-appointments";
    }


    @PostMapping("/appointments/{appointmentId}/reschedule")
    public String rescheduleAppointment(@PathVariable UUID appointmentId,
                                        @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
                                        @RequestParam("newTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime) {
        appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);
        return "redirect:/admin/manage-appointments";
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    public String cancelAppointment(@PathVariable UUID appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return "redirect:/admin/manage-appointments";
    }
}

