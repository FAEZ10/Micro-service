package com.microcommerce.clients.controller;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration", description = "API d'administration pour la gestion des clients")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final ClientService clientService;

    @Operation(
        summary = "Lister tous les clients",
        description = """
            Récupère la liste paginée de tous les clients du système. Accessible uniquement aux administrateurs.
            
            **Paramètres de pagination :**
            - `page` : Numéro de page (commence à 0)
            - `size` : Nombre d'éléments par page (max 100)
            - `sort` : Tri par propriété avec direction (ex: lastName,asc)
            
            **Propriétés de tri disponibles :**
            - `id` : ID du client
            - `email` : Adresse email
            - `firstName` : Prénom
            - `lastName` : Nom de famille
            - `role` : Rôle (CLIENT/ADMIN)
            - `active` : Statut actif
            - `createdAt` : Date de création
            - `updatedAt` : Date de modification
            
            **Exemples d'utilisation :**
            - Tri par nom : `sort=lastName,asc`
            - Tri par date : `sort=createdAt,desc`
            - Multi-tri : `sort=role,asc&sort=lastName,asc`
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste des clients récupérée avec succès",
            content = @Content(
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "Exemple de réponse",
                    description = "Page de clients avec pagination",
                    value = """
                    {
                      "content": [
                        {
                          "id": 1,
                          "email": "client@test.com",
                          "firstName": "Jean",
                          "lastName": "Dupont",
                          "phone": "0123456789",
                          "role": "CLIENT",
                          "active": true,
                          "createdAt": "2025-01-01T10:00:00",
                          "updatedAt": "2025-01-01T10:00:00"
                        }
                      ],
                      "pageable": {
                        "pageNumber": 0,
                        "pageSize": 20,
                        "sort": {
                          "sorted": true,
                          "empty": false
                        }
                      },
                      "totalElements": 1,
                      "totalPages": 1,
                      "first": true,
                      "last": true
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Paramètres de tri invalides",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Erreur de tri",
                    description = "Propriété de tri invalide",
                    value = """
                    {
                      "code": "INVALID_SORT_PROPERTY",
                      "message": "Propriété de tri invalide. Utilisez des propriétés valides comme: id, email, firstName, lastName, createdAt",
                      "timestamp": "2025-01-01T10:00:00",
                      "path": "/api/v1/admin/clients",
                      "status": 400
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès refusé - Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/clients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ClientResponse>> getAllClients(
            @Parameter(
                description = "Paramètres de pagination et tri",
                examples = {
                    @ExampleObject(
                        name = "Tri par nom",
                        description = "Trier par nom de famille croissant",
                        value = "page=0&size=10&sort=lastName,asc"
                    ),
                    @ExampleObject(
                        name = "Tri par date",
                        description = "Trier par date de création décroissante",
                        value = "page=0&size=20&sort=createdAt,desc"
                    ),
                    @ExampleObject(
                        name = "Multi-tri",
                        description = "Trier par rôle puis par nom",
                        value = "page=0&size=10&sort=role,asc&sort=lastName,asc"
                    )
                }
            )
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        
        log.info("Récupération de la liste des clients - Page: {}, Taille: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ClientResponse> clients = clientService.getAllClients(pageable);
        
        log.info("Liste des clients récupérée avec succès - {} clients trouvés", 
                clients.getTotalElements());
        
        return ResponseEntity.ok(clients);
    }

    @Operation(
        summary = "Promouvoir un client en administrateur",
        description = "Modifie le rôle d'un client pour lui donner les droits d'administrateur. Accessible uniquement aux administrateurs."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Client promu administrateur avec succès",
            content = @Content(schema = @Schema(implementation = ClientResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès refusé - Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Client non trouvé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/clients/{clientId}/promote-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> promoteToAdmin(
            @Parameter(description = "ID du client à promouvoir", required = true)
            @PathVariable Long clientId) {
        
        log.info("Promotion du client {} en administrateur", clientId);
        
        ClientResponse promotedClient = clientService.promoteToAdmin(clientId);
        
        log.info("Client {} promu administrateur avec succès", clientId);
        
        return ResponseEntity.ok(promotedClient);
    }

    @Operation(
        summary = "Rétrograder un administrateur en client",
        description = "Modifie le rôle d'un administrateur pour lui retirer les droits d'administration. Accessible uniquement aux administrateurs."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Administrateur rétrogradé en client avec succès",
            content = @Content(schema = @Schema(implementation = ClientResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès refusé - Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Client non trouvé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/clients/{clientId}/demote-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> demoteFromAdmin(
            @Parameter(description = "ID du client à rétrograder", required = true)
            @PathVariable Long clientId) {
        
        log.info("Rétrogradation de l'administrateur {} en client", clientId);
        
        ClientResponse demotedClient = clientService.demoteFromAdmin(clientId);
        
        log.info("Administrateur {} rétrogradé en client avec succès", clientId);
        
        return ResponseEntity.ok(demotedClient);
    }

    @Operation(
        summary = "Désactiver un client",
        description = "Désactive un compte client. Le client ne pourra plus se connecter. Accessible uniquement aux administrateurs."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Client désactivé avec succès",
            content = @Content(schema = @Schema(implementation = ClientResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès refusé - Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Client non trouvé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/clients/{clientId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> deactivateClient(
            @Parameter(description = "ID du client à désactiver", required = true)
            @PathVariable Long clientId) {
        
        log.info("Désactivation du client {}", clientId);
        
        ClientResponse deactivatedClient = clientService.deactivateClient(clientId);
        
        log.info("Client {} désactivé avec succès", clientId);
        
        return ResponseEntity.ok(deactivatedClient);
    }

    @Operation(
        summary = "Réactiver un client",
        description = "Réactive un compte client désactivé. Accessible uniquement aux administrateurs."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Client réactivé avec succès",
            content = @Content(schema = @Schema(implementation = ClientResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Non authentifié",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Accès refusé - Droits administrateur requis",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Client non trouvé",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PutMapping("/clients/{clientId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> activateClient(
            @Parameter(description = "ID du client à réactiver", required = true)
            @PathVariable Long clientId) {
        
        log.info("Réactivation du client {}", clientId);
        
        ClientResponse activatedClient = clientService.activateClient(clientId);
        
        log.info("Client {} réactivé avec succès", clientId);
        
        return ResponseEntity.ok(activatedClient);
    }
}
