package com.microcommerce.clients.controller;

import com.microcommerce.clients.dto.request.LoginRequest;
import com.microcommerce.clients.dto.request.RegisterRequest;
import com.microcommerce.clients.dto.response.AuthResponse;
import com.microcommerce.clients.dto.response.ErrorResponse;
import com.microcommerce.clients.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Endpoints pour l'inscription, la connexion et la gestion des tokens JWT")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Operation(
        summary = "Inscription d'un nouveau client",
        description = """
            Créer un nouveau compte client avec email et mot de passe.
            
            **Règles de validation :**
            - Email valide et unique
            - Mot de passe minimum 8 caractères
            - Confirmation du mot de passe obligatoire
            - Nom et prénom obligatoires
            
            **Réponse :**
            - Token JWT d'accès (15 minutes)
            - Token de rafraîchissement (7 jours)
            - Informations du client créé
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Client créé avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Erreurs de validation",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email déjà utilisé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Parameter(description = "Données d'inscription du client", required = true)
            @Valid @RequestBody RegisterRequest request) {
        
        logger.info("Registration attempt for email: {}", request.getEmail());
        
        AuthResponse response = authService.register(request);
        
        logger.info("Client registered successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Connexion d'un client",
        description = """
            Authentifier un client avec email et mot de passe.
            
            **Processus :**
            1. Validation des identifiants
            2. Vérification que le compte est actif
            3. Génération des tokens JWT
            
            **Réponse :**
            - Token JWT d'accès (15 minutes)
            - Token de rafraîchissement (7 jours)
            - Informations du client connecté
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Connexion réussie",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Erreurs de validation",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Identifiants incorrects",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Identifiants de connexion", required = true)
            @Valid @RequestBody LoginRequest request) {
        
        logger.info("Login attempt for email: {}", request.getEmail());
        
        AuthResponse response = authService.login(request);
        
        logger.info("Client logged in successfully: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Rafraîchir le token d'accès",
        description = """
            Obtenir un nouveau token d'accès en utilisant le token de rafraîchissement.
            
            **Utilisation :**
            - Quand le token d'accès expire (15 minutes)
            - Pour maintenir la session utilisateur
            - Évite de redemander les identifiants
            
            **Réponse :**
            - Nouveau token d'accès (15 minutes)
            - Nouveau token de rafraîchissement (7 jours)
            - Informations du client mises à jour
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token rafraîchi avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de rafraîchissement invalide ou expiré",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(
                description = "Token de rafraîchissement", 
                required = true,
                example = "eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestParam("refreshToken") String refreshToken) {
        
        logger.info("Token refresh attempt");
        
        AuthResponse response = authService.refreshToken(refreshToken);
        
        logger.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Valider un token d'accès",
        description = """
            Vérifier la validité d'un token d'accès JWT.
            
            **Utilisation :**
            - Validation côté client
            - Vérification avant appels API
            - Debugging des tokens
            
            **Réponse :**
            - `true` si le token est valide
            - `false` si le token est invalide ou expiré
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Validation effectuée",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "object", example = "{\"valid\": true}")
            )
        )
    })
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @Parameter(
                description = "Token d'accès à valider", 
                required = true,
                example = "eyJhbGciOiJIUzI1NiJ9..."
            )
            @RequestParam("token") String token) {
        
        boolean isValid = authService.validateAccessToken(token);
        
        return ResponseEntity.ok(new TokenValidationResponse(isValid));
    }


    public static class TokenValidationResponse {
        private boolean valid;

        public TokenValidationResponse(boolean valid) {
            this.valid = valid;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }
    }
}
