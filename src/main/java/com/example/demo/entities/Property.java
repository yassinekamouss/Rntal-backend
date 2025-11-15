package com.example.demo.entities;


import com.example.demo.enums.PropertyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal pricePerNight;
    @Enumerated(EnumType.STRING)
    private PropertyStatus status;

    /**
     * Relation to User entity representing the owner of the property.
     */
    @ManyToOne()
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * Relation to Rental entity representing the rentals associated with the property.
     */
     @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
     private List<Rental> rentals;

    /**
     * Relation to Image entity representing the images of the property.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "property_id")
    private List<Image> images;
}
