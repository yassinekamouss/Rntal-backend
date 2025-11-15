package com.example.demo.services;

import com.example.demo.dto.PropertyRequestDTO;
import com.example.demo.dto.PropertyResponseDTO;
import com.example.demo.entities.Image;
import com.example.demo.entities.Property;
import com.example.demo.entities.User;
import com.example.demo.enums.PropertyStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.exceptions.AppException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserService userService; // Pour trouver le propriétaire

    @Autowired
    private MapperService mapperService;

    @Transactional(readOnly = true)
    public List<PropertyResponseDTO> findAllProperties() {
        return propertyRepository.findAll().stream()
                .map(mapperService::toPropertyResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyResponseDTO> findProperties(String status, String q) {
        List<Property> properties;
        if (status != null && !status.isBlank()) {
            PropertyStatus st;
            try {
                st = PropertyStatus.valueOf(status.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new AppException("Statut de propriété invalide: " + status);
            }
            properties = propertyRepository.findByStatus(st);
            if (q != null && !q.isBlank()) {
                String qLower = q.toLowerCase(Locale.ROOT);
                properties = properties.stream()
                        .filter(p -> p.getAddress() != null && p.getAddress().toLowerCase(Locale.ROOT).contains(qLower))
                        .collect(Collectors.toList());
            }
        } else if (q != null && !q.isBlank()) {
            properties = propertyRepository.findByAddressContainingIgnoreCase(q);
        } else {
            properties = propertyRepository.findAll();
        }
        return properties.stream().map(mapperService::toPropertyResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyResponseDTO findPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propriété non trouvée avec l'ID : " + id));
        return mapperService.toPropertyResponseDTO(property);
    }

    @Transactional
    public PropertyResponseDTO createProperty(PropertyRequestDTO propertyRequest, Long ownerId) {
        // 1. Trouver le propriétaire
        User owner = userService.findUserByIdInternal(ownerId);

        // 2. Créer l'entité Property
        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(propertyRequest.getTitle());
        property.setDescription(propertyRequest.getDescription());
        property.setAddress(propertyRequest.getAddress());
        property.setLatitude(propertyRequest.getLatitude());
        property.setLongitude(propertyRequest.getLongitude());
        property.setPricePerNight(propertyRequest.getPricePerNight());

        // 3. Statut initial (logique métier de votre projet)
        property.setStatus(PropertyStatus.PENDING_VALIDATION);

        // 4. Gérer les images
        if (propertyRequest.getImageUrls() != null) {
            List<Image> images = propertyRequest.getImageUrls().stream()
                    .map(mapperService::toImageEntity)
                    .collect(Collectors.toList());
            property.setImages(images);
        }

        // 5. Sauvegarder
        Property savedProperty = propertyRepository.save(property);

        // 6. Retourner le DTO
        return mapperService.toPropertyResponseDTO(savedProperty);
    }

    @Transactional
    public PropertyResponseDTO updateProperty(Long id, PropertyRequestDTO propertyRequest, Long currentUserId) {
        // 1. Trouver la propriété existante
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propriété non trouvée avec l'ID : " + id));

        // 2. Vérification de sécurité: propriétaire ou ADMIN
        User current = userService.findUserByIdInternal(currentUserId);
        boolean isOwner = property.getOwner() != null && property.getOwner().getId().equals(currentUserId);
        boolean isAdmin = current.getRole() == UserRole.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cette propriété.");
        }

        // 3. Mettre à jour les champs
        property.setTitle(propertyRequest.getTitle());
        property.setDescription(propertyRequest.getDescription());
        property.setAddress(propertyRequest.getAddress());
        property.setLatitude(propertyRequest.getLatitude());
        property.setLongitude(propertyRequest.getLongitude());
        property.setPricePerNight(propertyRequest.getPricePerNight());

        // 4. Gérer la mise à jour des images (stratégie simple : remplacer)
        if (property.getImages() == null) {
            property.setImages(new ArrayList<>());
        }
        property.getImages().clear(); // Supprime les anciennes (grâce à orphanRemoval = true)
        if (propertyRequest.getImageUrls() != null) {
            List<Image> newImages = propertyRequest.getImageUrls().stream()
                    .map(mapperService::toImageEntity)
                    .collect(Collectors.toList());
            property.getImages().addAll(newImages);
        }

        // 5. Sauvegarder les modifications
        Property updatedProperty = propertyRepository.save(property);

        // 6. Retourner le DTO
        return mapperService.toPropertyResponseDTO(updatedProperty);
    }

    @Transactional
    public void deleteProperty(Long id, Long currentUserId) {
        // 1. Vérifier si la propriété existe et charger pour contrôle
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propriété non trouvée avec l'ID : " + id));

        // 2. Vérification de sécurité: propriétaire ou ADMIN
        User current = userService.findUserByIdInternal(currentUserId);
        boolean isOwner = property.getOwner() != null && property.getOwner().getId().equals(currentUserId);
        boolean isAdmin = current.getRole() == UserRole.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à supprimer cette propriété.");
        }

        // 3. Supprimer
        propertyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponseDTO> findPropertiesByOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId).stream()
                .map(mapperService::toPropertyResponseDTO)
                .collect(Collectors.toList());
    }
}