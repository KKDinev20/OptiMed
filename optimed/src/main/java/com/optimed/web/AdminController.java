package com.optimed.web;

import com.optimed.dto.UserRequest;
import com.optimed.entity.User;
import com.optimed.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public ModelAndView dashboard (Model model) {
        model.addAttribute ("totalUsers", userService.countUsers ());
        model.addAttribute ("currentPage", "Dashboard");
        model.addAttribute ("recentUsers", userService.getRecentUsers ());
        model.addAttribute ("totalAppointments", appointmentService.countAppointments ());
        return new ModelAndView ("admin/dashboard");
    }

    @GetMapping("/manage-users")
    public ModelAndView manageUsers (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<User> users = userService.getAllUsers (PageRequest.of (page, size));
        model.addAttribute ("users", users);
        model.addAttribute ("currentPage", "Manage Users");
        model.addAttribute ("pageSize", size);
        model.addAttribute ("pageSizes", Arrays.asList (5, 10, 15, 20));
        return new ModelAndView ("admin/manage-users");
    }

    @GetMapping("/edit-user/{userId}")
    public ModelAndView editUser (@PathVariable UUID userId, Model model) {
        model.addAttribute ("currentPage", "Edit User");
        model.addAttribute ("user", userService.getUserById (userId));
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

        User updatedUser = userService.updateUser (userId, userRequest);
        return "redirect:/admin/manage-users?updated=true";
    }
    /*@GetMapping("/manage-appointments")
    public ModelAndView manageAppointments(Model model) {
        model.addAttribute("currentPage", "Manage Appointments");
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return new ModelAndView("admin/manage-appointments");
    }*/

}
