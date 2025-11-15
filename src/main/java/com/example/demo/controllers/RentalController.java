package com.example.demo.controllers;

import com.example.demo.dto.RentalRequestDTO;
import com.example.demo.dto.RentalResponseDTO;
import com.example.demo.entities.User;
import com.example.demo.services.RentalService; // Vous devez créer ce service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rentals")
@CrossOrigin(origins = "http://localhost:4200")
public class RentalController {

    @Autowired
    private RentalService rentalService; // Service à créer

    /**
     * Endpoint sécurisé pour qu'un locataire (TENANT) crée une réservation.
     */
    @PreAuthorize("hasRole('TENANT')")
    @PostMapping
    public ResponseEntity<RentalResponseDTO> createRental(@AuthenticationPrincipal User currentUser,
                                                          @RequestBody RentalRequestDTO rentalRequest) {
        RentalResponseDTO createdRental = rentalService.createRental(rentalRequest, currentUser.getId());
        return new ResponseEntity<>(createdRental, HttpStatus.CREATED);
    }

    /**
     * Endpoint sécurisé pour qu'un locataire (TENANT) voie ses réservations.
     */
    @PreAuthorize("hasRole('TENANT')")
    @GetMapping("/my-rentals")
    public ResponseEntity<List<RentalResponseDTO>> getMyRentals(@AuthenticationPrincipal User currentUser) {
        List<RentalResponseDTO> rentals = rentalService.findRentalsByRenter(currentUser.getId());
        return ResponseEntity.ok(rentals);
    }

    /**
     * Endpoint sécurisé pour qu'un propriétaire (OWNER) voie les réservations
     * sur l'une de ses propriétés.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<RentalResponseDTO>> getRentalsForProperty(@PathVariable Long propertyId,
                                                                         @AuthenticationPrincipal User currentUser) {
        List<RentalResponseDTO> rentals = rentalService.findRentalsForProperty(propertyId, currentUser.getId());
        return ResponseEntity.ok(rentals);
    }
}