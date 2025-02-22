package com.optimed.dto;

import com.optimed.entity.enums.Role;
import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private Role role;
}
