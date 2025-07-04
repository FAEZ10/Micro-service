package com.microcommerce.clients.controller;

import com.microcommerce.clients.dto.request.RegisterRequest;
import com.microcommerce.clients.dto.response.ClientResponse;
import com.microcommerce.clients.dto.response.ErrorResponse;
import com.microcommerce.clients.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gestion des clients", description = "Endpoints pour la gestion des profils clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private ClientService clientService;

    @Operation(
        summary = "Obtenir son profil client",
        description = """
            Récupérer les informations du profil du client connecté.
            
            **Authentification requise :** Token JWT valide
            
            **Réponse :**
            - Informations complètes du profil
            - Adresses associées (via endpoint séparé)
            - Historique des modifications
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profil récupéré avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClientResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @GetMapping("/profile")
    public ResponseEntity<ClientResponse> getProfile(Authentication authentication) {
        String email = authentication.getName();
        logger.info("Profile request for client: {}", email);
        
        var client = clientService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        ClientResponse response = clientService.findById(client.getId());
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Mettre à jour son profil",
        description = """
            Modifier les informations du profil du client connecté.
            
            **Champs modifiables :**
            - Nom et prénom
            - Email (doit rester unique)
            - Téléphone
            - Mot de passe (optionnel)
            
            **Validation :**
            - Email unique dans le système
            - Mot de passe fort si fourni
            - Confirmation du mot de passe
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profil mis à jour avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClientResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    @PutMapping("/profile")
    public ResponseEntity<ClientResponse> updateProfile(
            @Parameter(description = "Nouvelles données du profil", required = true)
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication) {
        
        String email = authentication.getName();
        logger.info("Profile update request for client: {}", email);
        
        var client = clientService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        
        ClientResponse response = clientService.updateProfile(client.getId(), request);
        
        logger.info("Profile updated successfully for client: {}", email);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtenir un client par ID",
        description = """
            Récupérer les informations d'un client spécifique par son ID.
            
            **Accès :**
            - Admins : Tous les clients
            - Clients : Uniquement leur propre profil
            
            **Utilisation :**
            - Administration des comptes
            - Support client
            - Vérifications internes
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Client trouvé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClientResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "ID du client", required = true, example = "1")
            @PathVariable Long id) {
        
        logger.info("Client details request for ID: {}", id);
        
        ClientResponse response = clientService.findById(id);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Rechercher des clients",
        description = """
            Rechercher des clients avec pagination et filtres.
            
            **Accès :** Administrateurs uniquement
            
            **Paramètres de recherche :**
            - Texte libre (nom, prénom, email)
            - Pagination avec tri
            - Filtres par statut
            
            **Utilisation :**
            - Administration des comptes
            - Support client
            - Rapports et statistiques
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Résultats de recherche",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClientResponse>> searchClients(
            @Parameter(
                description = "Terme de recherche (nom, prénom, email)",
                examples = {
                    @ExampleObject(name = "Recherche par prénom", value = "jean"),
                    @ExampleObject(name = "Recherche par nom", value = "dupont"),
                    @ExampleObject(name = "Recherche par email", value = "client@test.com")
                }
            )
            @RequestParam(required = false) String search,
            
            @Parameter(
                description = "Numéro de page (commence à 0)",
                examples = {
                    @ExampleObject(name = "Première page", value = "0"),
                    @ExampleObject(name = "Deuxième page", value = "1")
                }
            )
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(
                description = "Nombre d'éléments par page (max 100)",
                examples = {
                    @ExampleObject(name = "Page standard", value = "20"),
                    @ExampleObject(name = "Page réduite", value = "10"),
                    @ExampleObject(name = "Page étendue", value = "50")
                }
            )
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(
                description = "Propriété de tri",
                examples = {
                    @ExampleObject(name = "Tri par nom", value = "lastName"),
                    @ExampleObject(name = "Tri par prénom", value = "firstName"),
                    @ExampleObject(name = "Tri par email", value = "email"),
                    @ExampleObject(name = "Tri par date", value = "createdAt"),
                    @ExampleObject(name = "Tri par rôle", value = "role")
                }
            )
            @RequestParam(defaultValue = "lastName") String sortBy,
            
            @Parameter(
                description = "Direction du tri",
                examples = {
                    @ExampleObject(name = "Croissant", value = "asc"),
                    @ExampleObject(name = "Décroissant", value = "desc")
                }
            )
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.info("Client search request - search: {}, page: {}, size: {}", search, page, size);
        
        // Validation de la taille de page
        if (size > 100) {
            size = 100;
            logger.warn("Page size limited to 100 elements");
        }
        
        // Validation des propriétés de tri autorisées
        String[] allowedSortProperties = {"id", "email", "firstName", "lastName", "role", "active", "createdAt", "updatedAt"};
        boolean isValidSortProperty = java.util.Arrays.asList(allowedSortProperties).contains(sortBy);
        if (!isValidSortProperty) {
            sortBy = "lastName";
            logger.warn("Invalid sort property, defaulting to lastName");
        }
        
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? 
            Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ClientResponse> clients = clientService.searchClients(search, pageable);
        
        logger.info("Found {} clients", clients.getTotalElements());
        return ResponseEntity.ok(clients);
    }

    @Operation(
        summary = "Obtenir les clients actifs",
        description = """
            Récupérer la liste des clients actifs avec pagination.
            
            **Accès :** Administrateurs uniquement
            
            **Utilisation :**
            - Rapports d'activité
            - Statistiques utilisateurs
            - Campagnes marketing
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste des clients actifs",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClientResponse>> getActiveClients(
            @Parameter(description = "Numéro de page (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Active clients request - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName"));
        Page<ClientResponse> clients = clientService.findActiveClients(pageable);
        
        return ResponseEntity.ok(clients);
    }

    @Operation(
        summary = "Activer/désactiver un client",
        description = """
            Changer le statut actif/inactif d'un client.
            
            **Accès :** Administrateurs uniquement
            
            **Effet :**
            - Client inactif : Ne peut plus se connecter
            - Client actif : Peut se connecter normalement
            
            **Utilisation :**
            - Suspension de compte
            - Modération
            - Gestion des accès
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statut modifié avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClientResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> toggleClientStatus(
            @Parameter(description = "ID du client", required = true, example = "1")
            @PathVariable Long id) {
        
        logger.info("Toggle status request for client ID: {}", id);
        
        ClientResponse response = clientService.toggleClientStatus(id);
        
        logger.info("Client ID: {} status changed to: {}", id, response.getActive());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Obtenir les statistiques des clients",
        description = """
            Récupérer les statistiques globales des clients.
            
            **Accès :** Administrateurs uniquement
            
            **Métriques :**
            - Nombre total de clients
            - Clients actifs/inactifs
            - Répartition par rôle
            - Tendances d'inscription
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Statistiques récupérées",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ClientService.ClientStats.class)
            )
        ),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientService.ClientStats> getClientStats() {
        logger.info("Client stats request");
        
        ClientService.ClientStats stats = clientService.getClientStats();
        
        return ResponseEntity.ok(stats);
    }
}
