package com.example.demo.dto;

import com.example.demo.enums.UserRole;
import lombok.Data;

@Data
public class UserResponseDTO {
    // Notez : PAS de champ 'password' !
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private UserRole role;
    private String walletAddress;
}