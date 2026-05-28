package com.pmp.dto;

import com.pmp.enumeration.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private Long userId;
    private String username;
    private Role role;
    private String message;
}
