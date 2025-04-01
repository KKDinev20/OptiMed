package com.optimed;

import com.optimed.entity.User;
import com.optimed.entity.enums.Role;
import com.optimed.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    private UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/dashboard/**", "/landing/**", "/static/**").permitAll()
                        .requestMatchers("/", "/auth/**", "/landing", "/dashboard").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/doctor/**").hasAuthority("ROLE_DOCTOR")
                        .requestMatchers("/patient/**").hasAuthority("ROLE_PATIENT")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/perform_login")
                        .successHandler(customSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher ("/logout"))
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .expiredUrl("/auth/login?expired=true")
                        .sessionRegistry(sessionRegistry())
                );

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            Optional<User> user = userService.findByUsername(username);

            if (user.isPresent() && !user.get().isEnabled () && user.get().getRole() != Role.ADMIN) {
                response.sendRedirect("/complete-profile");
                return;
            }

            switch (user.get().getRole()) {
                case ADMIN -> response.sendRedirect("/admin/dashboard");
                case DOCTOR -> response.sendRedirect("/doctor/dashboard");
                case PATIENT -> response.sendRedirect("/patient/dashboard");
            }
        };
    }



    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl ();
    }

    protected static boolean authenticateUserRole (HttpServletResponse response, Collection<? extends GrantedAuthority> authorities) throws IOException {
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            switch (role) {
                case "ROLE_ADMIN" -> {
                    response.sendRedirect ("/admin/dashboard");
                    return true;
                }
                case "ROLE_DOCTOR" -> {
                    response.sendRedirect ("/doctor/dashboard");
                    return true;
                }
                case "ROLE_PATIENT" -> {
                    response.sendRedirect ("/patient/dashboard");
                    return true;
                }
            }
        }
        return false;
    }
}