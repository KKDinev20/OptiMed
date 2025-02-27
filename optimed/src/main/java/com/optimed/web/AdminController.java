package com.optimed.web;

import com.optimed.dto.UserRequest;
import com.optimed.entity.Appointment;
import com.optimed.entity.User;
import com.optimed.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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


    @GetMapping("/manage-appointments")
    public ModelAndView manageAppointments (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter,
            Model model) {

        Page<Appointment> appointments = appointmentService.getAllAppointments (filter, PageRequest.of (page, size));

        model.addAttribute ("appointments", appointments);
        model.addAttribute ("filter", filter);

        return new ModelAndView ("admin/manage-appointments");
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
        return "admin/settings";
    }


    @PostMapping("/settings")
    public String updateUser (@AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("userRequest") UserRequest userRequest,
                              BindingResult bindingResult, Model model) {

        System.out.println ("Submitted data:");
        System.out.println ("Username: " + userRequest.getUsername ());
        System.out.println ("Email: " + userRequest.getEmail ());
        System.out.println ("Password: " + userRequest.getPassword ());
        System.out.println ("Role: " + userRequest.getRole ());

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

