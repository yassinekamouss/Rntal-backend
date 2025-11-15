package com.example.demo.controllers;

import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entities.User;
import com.example.demo.services.AuthenticationService;
import com.example.demo.services.MapperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") // Votre route de base est conservée
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Gardé pour le développement
public class AuthController {

    // Injection unique du service qui gère la logique
    private final AuthenticationService authService;
    private final MapperService mapperService;

    /**
     * Endpoint pour l'inscription.
     * Crée un nouvel utilisateur et renvoie un token JWT pour une connexion immédiate.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {
        // Le service s'occupe de :
        // 1. Valider la requête
        // 2. Créer l'utilisateur en BDD (en hachant le mot de passe)
        // 3. Générer un token JWT
        // 4. Renvoyer le token
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint pour la connexion.
     * Authentifie l'utilisateur et renvoie un token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request
    ) {
        // Le service s'occupe de :
        // 1. Authentifier via l'AuthenticationManager (qui utilise votre UserDetailsService)
        // 2. Si l'authentification réussit, générer un token JWT
        // 3. Renvoyer le token
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Retourne le profil de l'utilisateur connecté.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(mapperService.toUserResponseDTO(currentUser));
    }
}