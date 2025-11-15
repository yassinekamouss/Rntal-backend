package com.example.demo.dto;

import com.example.demo.enums.ReservationStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalResponseDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private ReservationStatus status;
    private String smartContractAddress;
    // On envoie les détails complets (mais filtrés) de la propriété
    private PropertyResponseDTO property;
    // On envoie les détails du locataire (sans mot de passe)
    private UserResponseDTO renter;
}