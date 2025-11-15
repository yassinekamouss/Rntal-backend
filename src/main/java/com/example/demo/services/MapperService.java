package com.example.demo.services;

import com.example.demo.dto.*;
import com.example.demo.entities.Image;
import com.example.demo.entities.Property;
import com.example.demo.entities.Rental;
import com.example.demo.entities.User;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class MapperService {

    //========== USER MAPPERS ==========

    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setWalletAddress(user.getWalletAddress());
        return dto;
    }

    //========== IMAGE MAPPERS ==========

    public ImageDTO toImageDTO(Image image) {
        if (image == null) return null;
        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setUrl(image.getUrl());
        return dto;
    }

    public Image toImageEntity(String url) {
        if (url == null) return null;
        Image image = new Image();
        image.setUrl(url);
        return image;
    }

    //========== PROPERTY MAPPERS ==========

    public PropertyResponseDTO toPropertyResponseDTO(Property property) {
        if (property == null) return null;
        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setAddress(property.getAddress());
        dto.setLatitude(property.getLatitude());
        dto.setLongitude(property.getLongitude());
        dto.setPricePerNight(property.getPricePerNight());
        dto.setStatus(property.getStatus());

        // Mapper le propriétaire (User -> UserResponseDTO)
        dto.setOwner(toUserResponseDTO(property.getOwner()));

        // Mapper la liste d'images (List<Image> -> List<ImageDTO>)
        if (property.getImages() != null) {
            dto.setImages(property.getImages().stream()
                    .map(this::toImageDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    //========== RENTAL MAPPERS ==========

    public RentalResponseDTO toRentalResponseDTO(Rental rental) {
        if (rental == null) return null;
        RentalResponseDTO dto = new RentalResponseDTO();
        dto.setId(rental.getId());
        dto.setStartDate(rental.getStartDate());
        dto.setEndDate(rental.getEndDate());
        dto.setTotalPrice(rental.getTotalPrice());
        dto.setStatus(rental.getStatus());
        dto.setSmartContractAddress(rental.getSmartContractAddress());

        // Mapper la propriété (Property -> PropertyResponseDTO)
        dto.setProperty(toPropertyResponseDTO(rental.getProperty()));

        // Mapper le locataire (User -> UserResponseDTO)
        dto.setRenter(toUserResponseDTO(rental.getRenter()));

        return dto;
    }
}