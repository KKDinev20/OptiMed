package com.optimed.security;

import com.optimed.entity.User;
import com.optimed.entity.enums.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/dashboard";

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals(Role.ADMIN.name())) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals(Role.DOCTOR.name())) {
                redirectUrl = "/doctor/dashboard";
                break;
            } else if (role.equals(Role.PATIENT.name())) {
                redirectUrl = "/patient/dashboard";
                break;
            }
        }

        System.out.println("Redirecting user to: " + redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}