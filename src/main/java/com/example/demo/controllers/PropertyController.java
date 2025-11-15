package com.example.demo.controllers;

import com.example.demo.dto.PropertyRequestDTO;
import com.example.demo.dto.PropertyResponseDTO;
import com.example.demo.entities.User;
import com.example.demo.services.PropertyService; // Vous devez créer ce service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
@CrossOrigin(origins = "http://localhost:4200")
public class PropertyController {

    @Autowired
    private PropertyService propertyService; // Service à créer

    /**
     * Endpoint public pour obtenir toutes les propriétés, avec filtres optionnels.
     */
    @GetMapping
    public ResponseEntity<List<PropertyResponseDTO>> getAllProperties(@RequestParam(required = false) String status,
                                                                      @RequestParam(required = false, name = "q") String query) {
        List<PropertyResponseDTO> properties = propertyService.findProperties(status, query);
        return ResponseEntity.ok(properties);
    }

    /**
     * Liste les propriétés de l’utilisateur courant (OWNER/ADMIN).
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<PropertyResponseDTO>> getMyProperties(@AuthenticationPrincipal User currentUser) {
        List<PropertyResponseDTO> properties = propertyService.findPropertiesByOwner(currentUser.getId());
        return ResponseEntity.ok(properties);
    }

    /**
     * Endpoint public pour obtenir une propriété par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getPropertyById(@PathVariable Long id) {
        // Le service lèvera une exception ResourceNotFoundException si non trouvé
        PropertyResponseDTO property = propertyService.findPropertyById(id);
        return ResponseEntity.ok(property);
    }

    /**
     * Endpoint sécurisé pour créer une nouvelle propriété.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PostMapping
    public ResponseEntity<PropertyResponseDTO> createProperty(@AuthenticationPrincipal User currentUser,
                                                              @RequestBody PropertyRequestDTO propertyRequest) {
        PropertyResponseDTO createdProperty = propertyService.createProperty(propertyRequest, currentUser.getId());
        return new ResponseEntity<>(createdProperty, HttpStatus.CREATED);
    }

    /**
     * Endpoint sécurisé pour mettre à jour une propriété.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> updateProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody PropertyRequestDTO propertyRequest) {

        PropertyResponseDTO updatedProperty = propertyService.updateProperty(id, propertyRequest, currentUser.getId());
        return ResponseEntity.ok(updatedProperty);
    }

    /**
     * Endpoint sécurisé pour supprimer une propriété.
     */
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id,
                                               @AuthenticationPrincipal User currentUser) {
        propertyService.deleteProperty(id, currentUser.getId());
        return ResponseEntity.noContent().build(); // Réponse 204 No Content
    }
}