package com.optimed.web;

import com.optimed.dto.*;
import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;


@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final DoctorService doctorService;
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
        stats.setPendingAppointments(statusStats.get("Pending"));
        stats.setBookedAppointments(statusStats.get("Booked"));
        stats.setConfirmedAppointments(statusStats.get("Confirmed"));

        return stats;
    }

    @GetMapping("/manage-users")
    public ModelAndView manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<User> users = userService.getAllUsers(PageRequest.of(page, size));

        model.addAttribute("users", users);
        model.addAttribute("currentPage", "Manage Users");
        model.addAttribute("pageSize", size);
        model.addAttribute("pageSizes", Arrays.asList(5, 10, 15, 20));

        return new ModelAndView("admin/manage-users");
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

        if (bindingResult.hasErrors ()) {
            model.addAttribute ("errors", bindingResult.getAllErrors ());
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


}

