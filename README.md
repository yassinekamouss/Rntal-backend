# Real Estate Rental Backend (Spring Boot 3)

## 1. Introduction
Ce dépôt contient le backend d'une application de location immobilière (Real Estate Rental). Il expose une API REST sécurisée par Spring Security + JWT permettant la gestion des utilisateurs, des propriétés (biens) et des locations (réservations). Ce README joue le rôle de rapport technique détaillé.

## 2. Objectifs & Portée
- Authentifier des utilisateurs (rôles: OWNER, TENANT, ADMIN).
- Permettre la publication et la gestion de biens immobiliers par les propriétaires.
- Offrir un catalogue public consultable (home + détail).
- Permettre aux locataires de créer des demandes de location dans des plages de dates valides.
- Imposer des règles métier (disponibilité, non-chevauchement des dates, impossibilité de louer son propre bien, statut de validation des propriétés).

## 3. Stack Technique
- Langage: Java 17
- Framework: Spring Boot 3.5.x
- Modules: Spring Web, Spring Data JPA, Spring Security
- Base de données: MySQL (DDL auto: `update`)
- Authentification: JWT (HS256)
- Build: Maven Wrapper
- Lombok pour réduire le boilerplate

## 4. Architecture & Structure des Packages
```
com.example.demo
 ├─ config/        (Sécurité, filtres, beans)
 ├─ controllers/   (Endpoints REST)
 ├─ dto/           (Objets d'échange API)
 ├─ entities/      (Modèles JPA)
 ├─ enums/         (Types métiers)
 ├─ exceptions/    (Exceptions + Global handler)
 ├─ repositories/  (Interfaces Spring Data)
 └─ services/      (Logique métier + mapping)
```
Principes suivis:
- Controllers minces déléguant aux services.
- Services transactionnels (`@Transactional`) pour garantir cohérence.
- DTOs pour isoler le modèle interne.
- MapperService centralise la conversion entité ↔ DTO.

## 5. Sécurité & Authentification
- JWT stateless (aucune session serveur).
- Filtre custom `JwtAuthenticationFilter` extrait le token dans l'en-tête `Authorization: Bearer <token>`.
- `UserDetailsService` charge l'utilisateur par email.
- `SecurityConfig` définit les règles:
  - Public: `POST /auth/register`, `POST /auth/login`, `GET /properties/**`.
  - Protégé: tout le reste (token requis).
  - Autorisations fines via `@PreAuthorize` dans les controllers/services.
- Les rôles stockés en base au format `ROLE_OWNER`, `ROLE_TENANT`, `ROLE_ADMIN`.

## 6. Cycle de Vie Auth (Login / Requête protégée)
1. Login: POST /auth/login → Authentification via `AuthenticationManager` → Génération JWT (10h par défaut).
2. Front stocke le token localement (localStorage / memory).
3. Requête protégée: Ajout de l'en-tête Bearer → Filtre JWT valide la signature + expiration → Construit un `Authentication` injecté dans le contexte.
4. Accès contrôlé par `@PreAuthorize` et les règles de path.

## 7. Entités Principales (simplifiées)
- User: (id, firstname, lastname, email unique, password hashé, role, walletAddress, properties[], rentals[])
- Property: (id, title, description, address, latitude, longitude, pricePerNight, status, owner, images[], rentals[])
- Rental: (id, startDate, endDate, totalPrice, status, smartContractAddress, property, renter)
- Image: (id, url)

### Statuts / Enums
- PropertyStatus: AVAILABLE, RENTED, PENDING_VALIDATION
- ReservationStatus: PENDING_CONFIRMATION, CONFIRMED, COMPLETED, CANCELLED
- UserRole: ROLE_OWNER, ROLE_TENANT, ROLE_ADMIN

## 8. DTOs (Contrat d'API)
- RegisterRequestDTO: firstname, lastname, email, password, role?, walletAddress?
- LoginRequestDTO: email, password
- AuthResponseDTO: token
- UserResponseDTO: id, firstname, lastname, email, role, walletAddress
- PropertyRequestDTO: title, description, address, latitude, longitude, pricePerNight, imageUrls[]
- PropertyResponseDTO: id, title, description, address, latitude, longitude, pricePerNight, status, owner(UserResponseDTO), images(ImageDTO[])
- ImageDTO: id, url
- RentalRequestDTO: propertyId, startDate, endDate
- RentalResponseDTO: id, startDate, endDate, totalPrice, status, smartContractAddress, property(PropertyResponseDTO), renter(UserResponseDTO)

## 9. Endpoints & Autorisations
Base URL: `/api`

### Auth
- POST `/auth/register` (Public) → Crée compte + renvoie JWT
- POST `/auth/login` (Public) → Authentifie + renvoie JWT
- GET  `/auth/me` (Authentifié) → Profil courant

### Properties
- GET `/properties` (Public) Query: `status`, `q` (recherche adresse)
- GET `/properties/{id}` (Public)
- GET `/properties/my` (OWNER or ADMIN) → Biens de l'utilisateur
- POST `/properties` (OWNER or ADMIN) → Créer (statut initial PENDING_VALIDATION)
- PUT `/properties/{id}` (OWNER or ADMIN & propriétaire ou ADMIN) → Modifier
- DELETE `/properties/{id}` (OWNER or ADMIN & propriétaire ou ADMIN) → Supprimer

### Rentals
- POST `/rentals` (TENANT) → Créer réservation
- GET  `/rentals/my-rentals` (TENANT) → Locations du locataire
- GET  `/rentals/property/{propertyId}` (OWNER or ADMIN & propriétaire) → Locations pour un bien

## 10. Règles Métier Clés
- Une propriété doit être `AVAILABLE` pour être louée.
- Un propriétaire ne peut pas louer son propre bien.
- Dates de location: `startDate >= aujourd'hui` et `endDate >= startDate + 1 jour`.
- Chevauchement interdit: pas de location dont l'intervalle intersecte une existante (test sur start/end).
- Statut initial d'une propriété: `PENDING_VALIDATION` (peut être validée ensuite, future évolution).

## 11. Gestion des Erreurs (GlobalExceptionHandler)
- 404: ResourceNotFoundException → { message, details }
- 400: AppException / IllegalArgumentException → { message, details }
- 403: AccessDeniedException → { message, details }
- 500: Exception générique → { message: "Une erreur interne...", error }

## 12. Configuration (extrait `application.properties`)
```
spring.application.name=RentalService
server.servlet.context-path=/api
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/rentaldb
spring.datasource.username=rentaldbuser
spring.datasource.password=OotoGrn9SWm5X)Ze
spring.jpa.hibernate.ddl-auto=update
jwt.secret-key=VOTRE_TRES_LONGUE_CLE_SECRETE_DE_256_BITS_ICI_VOTRE_TRES_LONGUE_CLE_SECRETE_DE_256_BITS_ICI
```
Recommandation: externaliser le mot de passe DB et la clé JWT dans des variables d'environnement ou un vault (ne pas commiter en clair).

## 13. Build & Run
Pré-requis: Java 17, MySQL démarré avec base `rentaldb`.

Commande Maven (packaging sans tests):
```
./mvnw -DskipTests package
```
Lancer l'application:
```
./mvnw spring-boot:run
```
Le service écoute sur `http://localhost:8080/api`.

## 14. Exemples d'Utilisation (curl)
Inscription:
```
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"firstname":"Alice","lastname":"Owner","email":"alice@ex.com","password":"Secret123","role":"ROLE_OWNER"}'
```
Login:
```
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@ex.com","password":"Secret123"}' | jq -r .token)
```
Liste des propriétés disponibles:
```
curl http://localhost:8080/api/properties?status=AVAILABLE
```
Création d'une propriété (OWNER):
```
curl -X POST http://localhost:8080/api/properties \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Studio cosy","description":"Calme et lumineux","address":"12 Rue Victor","latitude":48.85,"longitude":2.34,"pricePerNight":75.00,"imageUrls":["https://ex.com/img1.jpg"]}'
```
Création d'une location (TENANT):
```
curl -X POST http://localhost:8080/api/rentals \
  -H "Authorization: Bearer $TOKEN_TENANT" -H "Content-Type: application/json" \
  -d '{"propertyId":1,"startDate":"2025-12-01","endDate":"2025-12-05"}'
```

## 15. Qualité & Sécurité
- Stateless JWT réduit la surface d'attaque liée aux sessions.
- Passwords hashés BCrypt.
- Autorisations multi-niveaux (paths + méthodes).
- Validation métier centralisée dans les services.

## 16. Améliorations Futures (Backlog)
- Validation d'une propriété par ADMIN (endpoint status update).
- Pagination / tri sur `GET /properties`.
- Ajout de filtres supplémentaires (prix min/max, géolocalisation rectangulaire).
- Réservation confirmée / flux de paiement / intégration smart contract (champ `smartContractAddress`).
- WebSocket / SSE pour notifications (confirmation, annulation).
- Système de revue / rating des propriétés.
- Audit (création/modification) & soft delete.
- Tests d'intégration (Testcontainers MySQL) & couverture plus élevée.

## 17. Tests
Actuellement: Pas de suite de tests fournie. Recommandations:
- Unit tests: Services (PropertyService, RentalService) avec règles métier.
- Test sécurité: accès endpoints selon rôle (Spring Security Test).
- Integration tests: scénario end-to-end (création user, login, création propriété, location).

## 18. Observabilité (À prévoir)
- Intégration future: Spring Boot Actuator pour health-check, metrics.
- Logging structuré (JSON) pour ingestion ELK / OpenSearch.

## 19. Conformité & Sécurité des Données
- Les mots de passe ne sont jamais renvoyés dans les réponses.
- Attention RGPD: prévoir suppression / anonymisation des données à la demande.
- Clé JWT et credentials DB doivent être externalisés hors du code.

## 20. Résumé Exécutif
Le backend est prêt pour être consommé par un frontend moderne (Angular ou autre). Il fournit une API claire, sécurisée, extensible et basée sur des patterns éprouvés (layered architecture, DTOs, stateless auth). Les prochaines étapes prioritaires sont: pagination, endpoint de validation des propriétés et ajout de tests automatisés.

---

