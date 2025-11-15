package com.example.demo.dto;

import com.example.demo.enums.PropertyStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal pricePerNight;
    private PropertyStatus status;
    // On envoie un DTO du propriétaire (sans mot de passe)
    private UserResponseDTO owner;
    // On envoie une liste de DTOs d'images
    private List<ImageDTO> images;
}