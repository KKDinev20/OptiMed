package com.optimed.entity.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
public class AuthenticationController {

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("auth/login");
    }

    @GetMapping("/register")
    public ModelAndView register() {
        return new ModelAndView("auth/register");
    }


}
