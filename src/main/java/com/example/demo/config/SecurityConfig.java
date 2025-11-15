package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Pour @PreAuthorize sur les méthodes
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider; // Configuré dans ApplicationConfig

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 1. Désactiver CSRF (non nécessaire pour les API REST stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Définir les règles d'autorisation
                .authorizeHttpRequests(authz -> authz
                        // Auth public: uniquement register et login
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                        // Lecture publique des propriétés (Home + Détail)
                        .requestMatchers(HttpMethod.GET, "/properties/**").permitAll()
                        // Tout le reste nécessite une authentification (création location, CRUD properties, etc.)
                        .anyRequest().authenticated()
                )

                // 3. Définir la gestion de session comme STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Indiquer quel AuthenticationProvider utiliser
                .authenticationProvider(authenticationProvider)

                // 5. Ajouter le filtre JWT avant UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autorisez votre frontend Angular
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));

        // Autorisez toutes les méthodes (GET, POST, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Autorisez tous les en-têtes (important pour "Content-Type", "Authorization")
        configuration.setAllowedHeaders(List.of("*"));

        // Autorisez les credentials (cookies, etc., si nécessaire)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Appliquez cette configuration à toutes les routes
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}