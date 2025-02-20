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
    public ModelAndView login(@RequestParam(value = "error", required = false) String error) {
        ModelAndView mav = new ModelAndView("auth/login");
        if (error != null) {
            mav.addObject("errorMessage", "Invalid username or password.");
        }
        return mav;
    }

    @GetMapping("/register")
    public ModelAndView register() {
        ModelAndView mav = new ModelAndView("auth/register");
        mav.addObject("registerRequest", new RegisterRequest ());
        return mav;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest userRegistrationDto,
                                     BindingResult bindingResult,
                                     HttpSession session) {
        ModelAndView mav = new ModelAndView();

        if (bindingResult.hasErrors()) {
            mav.setViewName("auth/register");
            return mav;
        }

        if (userService.findByUsername (userRegistrationDto.getUsername ()).isPresent ()) {
            mav.setViewName("auth/register");
            mav.addObject("errorMessage", "Username already exists");
            return mav;
        }

        userService.registerUser(userRegistrationDto);

        session.setAttribute("currentUser", userRegistrationDto.getUsername());

        mav.setViewName("redirect:/dashboard");
        return mav;
    }


}
