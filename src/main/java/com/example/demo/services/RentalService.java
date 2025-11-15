package com.example.demo.services;

import com.example.demo.dto.RentalRequestDTO;
import com.example.demo.dto.RentalResponseDTO;
import com.example.demo.entities.Property;
import com.example.demo.entities.Rental;
import com.example.demo.entities.User;
import com.example.demo.enums.PropertyStatus;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exceptions.AppException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.PropertyRepository;
import com.example.demo.repositories.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MapperService mapperService;

    @Transactional
    public RentalResponseDTO createRental(RentalRequestDTO rentalRequest, Long renterId) {
        // 1. Valider les dates
        if (rentalRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new AppException("La date de début ne peut pas être dans le passé.");
        }
        if (rentalRequest.getEndDate().isBefore(rentalRequest.getStartDate().plusDays(1))) {
            throw new AppException("La date de fin doit être au moins un jour après la date de début.");
        }

        // 2. Trouver le locataire et la propriété
        User renter = userService.findUserByIdInternal(renterId);
        Property property = propertyRepository.findById(rentalRequest.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Propriété non trouvée."));

        // 2b. Empêcher de louer son propre bien
        if (property.getOwner() != null && property.getOwner().getId().equals(renterId)) {
            throw new AppException("Vous ne pouvez pas louer votre propre bien.");
        }

        // 3. Logique métier : Vérifier la disponibilité
        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new AppException("Cette propriété n'est pas disponible à la location.");
        }

        // 4. Logique métier : Vérifier les conflits de dates
        // On cherche les locations qui se terminent *après* le début de ma demande
        List<Rental> overlappingRentals = rentalRepository.findActiveRentalsForProperty(
                property.getId(), rentalRequest.getStartDate());

        boolean isOverlap = overlappingRentals.stream().anyMatch(rental ->
                // Soit la location existante commence avant la fin de ma demande
                rental.getStartDate().isBefore(rentalRequest.getEndDate())
        );

        if (isOverlap) {
            throw new AppException("Ces dates ne sont pas disponibles pour cette propriété.");
        }

        // 5. Logique métier : Calculer le prix
        long numberOfNights = ChronoUnit.DAYS.between(rentalRequest.getStartDate(), rentalRequest.getEndDate());
        BigDecimal totalPrice = property.getPricePerNight().multiply(new BigDecimal(numberOfNights));

        // 6. Créer l'entité Rental
        Rental rental = new Rental();
        rental.setRenter(renter);
        rental.setProperty(property);
        rental.setStartDate(rentalRequest.getStartDate());
        rental.setEndDate(rentalRequest.getEndDate());
        rental.setTotalPrice(totalPrice);
        rental.setStatus(ReservationStatus.PENDING_CONFIRMATION); // Statut initial

        // 7. Sauvegarder
        Rental savedRental = rentalRepository.save(rental);

        // 8. Retourner le DTO
        return mapperService.toRentalResponseDTO(savedRental);
    }

    @Transactional(readOnly = true)
    public List<RentalResponseDTO> findRentalsByRenter(Long renterId) {
        // Vérifie que l'utilisateur existe
        userService.findUserByIdInternal(renterId);

        return rentalRepository.findByRenterId(renterId).stream()
                .map(mapperService::toRentalResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RentalResponseDTO> findRentalsForProperty(Long propertyId, Long ownerId) {
        // 1. Vérifier que la propriété existe
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Propriété non trouvée."));

        // 2. Vérification de sécurité
        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à voir les locations de cette propriété.");
        }

        // 3. Récupérer et mapper les locations
        return rentalRepository.findByPropertyId(propertyId).stream()
                .map(mapperService::toRentalResponseDTO)
                .collect(Collectors.toList());
    }
}