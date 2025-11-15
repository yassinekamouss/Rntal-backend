package com.example.demo.entities;

import com.example.demo.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private String smartContractAddress;

    /**
     * Relation to Property entity representing the rented property.
     */
    @ManyToOne()
    @JoinColumn(name = "property_id")
    private Property property;

    /**
     * Relation to User entity representing the renter.
     */

    @ManyToOne()
    @JoinColumn(name = "renter_id")
    private User renter;

}
