package com.example.demo.repositories;

import com.example.demo.entities.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    /**
     * Trouve toutes les locations faites par un locataire (renter) spécifique.
     * Essentiel pour la page "Mes locations" ou "Mes voyages".
     */
    List<Rental> findByRenterId(Long renterId);

    /**
     * Trouve toutes les locations associées à une propriété spécifique.
     * Utile pour un propriétaire qui veut voir l'historique des locations de son bien.
     */
    List<Rental> findByPropertyId(Long propertyId);

    /**
     * Trouve toutes les locations pour une propriété qui ne sont pas encore terminées.
     * C'est la logique de base pour vérifier la disponibilité d'une propriété.
     * (Ici, j'utilise @Query pour vous montrer une alternative à la simple
     * nomination de méthode, car la logique est plus complexe).
     */
    @Query("SELECT r FROM Rental r WHERE r.property.id = :propertyId AND r.endDate > :checkDate")
    List<Rental> findActiveRentalsForProperty(Long propertyId, LocalDate checkDate);

}
