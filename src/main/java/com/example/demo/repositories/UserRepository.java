package com.example.demo.repositories;

import com.example.demo.entities.User;
import com.example.demo.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    /**
     * Trouve un utilisateur par son email.
     * Essentiel pour la connexion (login) et pour vérifier si un email existe déjà.
     * Spring comprend "findByEmail" et génère : "SELECT * FROM user WHERE email = ?"
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve tous les utilisateurs ayant un certain rôle.
     * Utile pour un admin qui veut lister tous les propriétaires (OWNER) ou locataires (TENANT).
     */
    List<User> findByRole(UserRole role);

}
