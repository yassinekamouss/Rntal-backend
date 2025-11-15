package com.example.demo.services;

import com.example.demo.entities.User;
import com.example.demo.enums.UserRole;
import com.example.demo.repositories.UserRepository;
import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.RegisterRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Bean de SecurityBeans
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Gère l'inscription d'un nouvel utilisateur.
     */
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // 1. Créer l'objet User à partir du DTO
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.ROLE_TENANT)
                .walletAddress(request.getWalletAddress())
                .build();

        // 2. Sauvegarder l'utilisateur en BDD
        userRepository.save(user);

        // 3. Générer le token JWT pour cet utilisateur
        String jwtToken = jwtService.generateToken(user);

        // 4. Renvoyer le token
        return new AuthResponseDTO(jwtToken);
    }

    /**
     * Gère la connexion d'un utilisateur existant.
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        // 1. Authentifier l'utilisateur (email comme identifiant)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Charger l'utilisateur après authentification
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé après authentification"));

        // 3. Générer le token JWT
        String jwtToken = jwtService.generateToken(user);

        // 4. Renvoyer le token
        return new AuthResponseDTO(jwtToken);
    }
}