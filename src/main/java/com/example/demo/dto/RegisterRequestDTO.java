package com.example.demo.dto;

import com.example.demo.enums.UserRole;
import lombok.Data;

@Data // Génère getters, setters, toString, etc.
public class RegisterRequestDTO {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private UserRole role;
    private String walletAddress;
}