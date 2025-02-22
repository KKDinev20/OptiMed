package com.optimed.web;

import com.optimed.dto.RegisterRequest;
import com.optimed.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logoutPage(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public ModelAndView register() {
        ModelAndView mav = new ModelAndView("auth/register");
        mav.addObject("registerRequest", new RegisterRequest());
        return mav;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                     BindingResult bindingResult,
                                     HttpSession session) {
        ModelAndView mav = new ModelAndView();

        if (bindingResult.hasErrors()) {
            mav.setViewName("auth/register");
            return mav;
        }

        if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
            mav.setViewName("auth/register");
            mav.addObject("errorMessage", "Username already exists");
            return mav;
        }

        userService.registerUser(registerRequest);
        session.setAttribute("currentUser", registerRequest.getUsername());
        mav.setViewName("redirect:/dashboard");
        return mav;
    }
}
