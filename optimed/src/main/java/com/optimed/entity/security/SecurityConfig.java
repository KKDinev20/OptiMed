package com.optimed.entity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("dashboard/**", "landing/**", "/static/**").permitAll()
                        .requestMatchers("/", "/auth/**", "/landing", "/dashboard").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
