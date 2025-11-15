package com.example.demo.services;

import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.dto.UserResponseDTO;
import com.example.demo.entities.User;
import com.example.demo.exceptions.AppException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MapperService mapperService;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }

    @Transactional(readOnly = true)
    public User findUserByIdInternal(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
    }

    @Transactional
    public UserResponseDTO registerUser(RegisterRequestDTO registerRequest) {
        // 1. Vérifier si l'email existe déjà
        userRepository.findByEmail(registerRequest.getEmail()).ifPresent(user -> {
            throw new AppException("Un compte existe déjà avec cet email.");
        });

        // 2. Créer l'entité User
        User user = new User();
        user.setFirstname(registerRequest.getFirstname());
        user.setLastname(registerRequest.getLastname());
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());
        user.setWalletAddress(registerRequest.getWalletAddress());

        // 3. Hacher le mot de passe
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // 4. Sauvegarder l'entité
        User savedUser = userRepository.save(user);

        // 5. Convertir en DTO de réponse et retourner
        return mapperService.toUserResponseDTO(savedUser);
    }
}