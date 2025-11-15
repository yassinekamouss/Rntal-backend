package com.example.demo.repositories;

import com.example.demo.entities.Property;
import com.example.demo.enums.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * Trouve toutes les propriétés appartenant à un propriétaire spécifique.
     * Essentiel pour la page "Mes propriétés" d'un propriétaire.
     */
    List<Property> findByOwnerId(Long ownerId);

    /**
     * Trouve toutes les propriétés ayant un certain statut.
     * Essentiel pour la "validation des propriétés" : un admin cherchera
     * toutes les propriétés avec le statut "PENDING_VALIDATION".
     */
    List<Property> findByStatus(PropertyStatus status);

    /**
     * Trouve les propriétés dont l'adresse contient un mot-clé (pour une barre de recherche).
     * Le "Containing" se traduit par un "LIKE %motCle%" en SQL.
     */
    List<Property> findByAddressContainingIgnoreCase(String keyword);

}
