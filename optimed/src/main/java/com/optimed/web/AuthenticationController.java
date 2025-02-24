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
    public String login () {
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logoutPage (HttpSession session) {
        session.invalidate ();
        return "redirect:/";
    }

    @GetMapping("/register")
    public ModelAndView register () {
        ModelAndView modelAndView = new ModelAndView ("auth/register");
        modelAndView.addObject ("registerRequest", new RegisterRequest ());
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerUser (@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                      BindingResult bindingResult,
                                      HttpSession session) {
        ModelAndView modelAndView = new ModelAndView ();

        if (bindingResult.hasErrors ()) {
            modelAndView.setViewName ("auth/register");
            return modelAndView;
        }

        if (userService.findByUsername (registerRequest.getUsername ()).isPresent ()) {
            modelAndView.setViewName ("auth/register");
            modelAndView.addObject ("errorMessage", "Username already exists");
            return modelAndView;
        }

        userService.registerUser (registerRequest);
        session.setAttribute ("currentUser", registerRequest.getUsername ());
        modelAndView.setViewName ("redirect:/dashboard");
        return modelAndView;
    }
}
