package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyRequestDTO {
    private String title;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal pricePerNight;
    // Le client envoie juste les URLs, le backend crée les entités Image
    private List<String> imageUrls;
}