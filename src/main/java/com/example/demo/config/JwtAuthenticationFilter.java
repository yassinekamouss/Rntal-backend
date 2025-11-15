package com.example.demo.config;

import com.example.demo.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component // Très important : doit être un Bean Spring
@RequiredArgsConstructor // Pour l'injection de dépendances
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Récupérer l'en-tête "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Vérifier si l'en-tête existe et commence bien par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Passe au filtre suivant
            return;
        }

        // 3. Extraire le token (il commence après "Bearer ")
        jwt = authHeader.substring(7);

        // 4. Extraire le username du token (via JwtService)
        username = jwtService.extractUsername(jwt);

        // 5. Vérifier si l'utilisateur est déjà authentifié dans le contexte de sécurité
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Charger les détails de l'utilisateur depuis la BDD (via UserDetailsService)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. Valider le token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 8. Créer un "Token" d'authentification Spring
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // On n'a pas besoin des credentials ici
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Mettre à jour le Contexte de Sécurité (C'EST LA LIGNE CLÉ !)
                // L'utilisateur est maintenant considéré comme authentifié pour cette requête.
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 10. Passer la main au filtre suivant dans la chaîne
        filterChain.doFilter(request, response);
    }
}