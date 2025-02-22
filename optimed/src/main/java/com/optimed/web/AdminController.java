package com.optimed.web;

import com.optimed.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Model model) {
        model.addAttribute("totalUsers", userService.countUsers ());
        model.addAttribute("recentUsers", userService.getRecentUsers());
        model.addAttribute("totalAppointments", appointmentService.countAppointments());
        return new ModelAndView("admin/dashboard");
    }

    /*@GetMapping("/manage-users")
    public ModelAndView manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return new ModelAndView("admin/manage-users");
    }

    @GetMapping("/edit-user/{userId}")
    public ModelAndView editUser(@PathVariable Long userId, Model model) {
        model.addAttribute("user", userService.getUserById(userId));
        return new ModelAndView("admin/edit-user");
    }

    @PostMapping("/edit-user/{userId}")
    public String updateUser(@PathVariable Long userId, @ModelAttribute("user") Object userDto) {
        userService.updateUser(userId, userDto);
        return "redirect:/admin/manage-users?updated=true";
    }

    @GetMapping("/manage-appointments")
    public ModelAndView manageAppointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        return new ModelAndView("admin/manage-appointments");
    }*/

}
