package com.optimed.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotEmpty(message = "Username is required")
    @Size(min = 3, max = 25, message = "Username must be atleast 3 symbols.")
    private String username;

    @NotEmpty(message = "Password is required")
    @Size(min = 6, max = 12, message = "Password must be atleast 6 symbols.")
    private String password;

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email is required")
    private String email;
}