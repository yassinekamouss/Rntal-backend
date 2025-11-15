package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RentalRequestDTO {
    private Long propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
}