package com.microcommerce.orders.controller;

import com.microcommerce.orders.dto.request.AddToCartRequest;
import com.microcommerce.orders.dto.response.OrderResponse;
import com.microcommerce.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Commandes", description = "API de gestion des commandes et du panier")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ===== Gestion du panier =====

    @GetMapping("/cart")
    @Operation(summary = "Récupérer le panier", 
               description = "Récupère le panier actuel du client connecté ou en crée un nouveau s'il n'existe pas")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Panier récupéré avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<OrderResponse> getCart(HttpServletRequest request) {
        Long clientId = (Long) request.getAttribute("userId");
        log.info("Récupération du panier pour le client: {}", clientId);
        
        OrderResponse cart = orderService.getOrCreateCart(clientId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/cart/add")
    @Operation(summary = "Ajouter un produit au panier", 
               description = "Ajoute un produit au panier du client connecté")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produit ajouté au panier avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<OrderResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {
        
        Long clientId = (Long) httpRequest.getAttribute("userId");
        log.info("Ajout au panier - Client: {}, Produit: {}", clientId, request.getProductId());
        
        OrderResponse updatedCart = orderService.addToCart(clientId, request);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/cart/remove/{productId}")
    @Operation(summary = "Supprimer un produit du panier", 
               description = "Supprime un produit du panier du client connecté")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produit supprimé du panier avec succès"),
        @ApiResponse(responseCode = "400", description = "Produit non trouvé dans le panier"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Panier non trouvé"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<OrderResponse> removeFromCart(
            @Parameter(description = "ID du produit à supprimer") @PathVariable Long productId,
            HttpServletRequest request) {
        
        Long clientId = (Long) request.getAttribute("userId");
        log.info("Suppression du panier - Client: {}, Produit: {}", clientId, productId);
        
        OrderResponse updatedCart = orderService.removeFromCart(clientId, productId);
        return ResponseEntity.ok(updatedCart);
    }

    // ===== Gestion des commandes =====

    @GetMapping("/{orderId}")
    @Operation(summary = "Récupérer une commande par ID", 
               description = "Récupère les détails d'une commande spécifique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Commande trouvée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvée"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.principal)")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "ID de la commande") @PathVariable Long orderId) {
        
        log.info("Récupération de la commande: {}", orderId);
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @Operation(
        summary = "Récupérer mes commandes", 
        description = """
            Récupère toutes les commandes du client connecté avec pagination et tri.
            
            **Accès :** Authentifié (client connecté uniquement)
            
            **Paramètres de pagination :**
            - `page` : Numéro de page (commence à 0)
            - `size` : Nombre d'éléments par page (max 100)
            - `sort` : Tri par propriété avec direction (ex: createdAt,desc)
            
            **Propriétés de tri disponibles :**
            - `id` : ID de la commande
            - `status` : Statut de la commande
            - `totalAmount` : Montant total
            - `createdAt` : Date de création
            - `updatedAt` : Date de modification
            
            **Exemples d'utilisation :**
            - Tri par date : `sort=createdAt,desc`
            - Tri par montant : `sort=totalAmount,desc`
            - Multi-tri : `sort=status,asc&sort=createdAt,desc`
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Commandes récupérées avec succès"),
        @ApiResponse(responseCode = "400", description = "Paramètres de pagination ou tri invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Tri (format: propriété,direction)", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            HttpServletRequest request) {
        
        Long clientId = (Long) request.getAttribute("userId");
        log.info("Récupération des commandes pour le client: {} - Page: {}, Taille: {}, Sort: {}", 
                clientId, page, size, sort);
        
        Page<OrderResponse> orders = orderService.getOrdersByClient(clientId, page, size, sort);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/validate")
    @Operation(summary = "Valider une commande", 
               description = "Valide une commande en statut CART pour la transformer en commande")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Commande validée avec succès"),
        @ApiResponse(responseCode = "400", description = "Commande ne peut pas être validée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Commande non trouvée"),
        @ApiResponse(responseCode = "500", description = "Erreur interne du serveur")
    })
    @PreAuthorize("hasRole('ADMIN') or @orderService.isOrderOwner(#orderId, authentication.principal)")
    public ResponseEntity<OrderResponse> validateOrder(
            @Parameter(description = "ID de la commande à valider") @PathVariable Long orderId) {
        
        log.info("Validation de la commande: {}", orderId);
        OrderResponse validatedOrder = orderService.validateOrder(orderId);
        return ResponseEntity.ok(validatedOrder);
    }
}
