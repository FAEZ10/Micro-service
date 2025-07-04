package com.microcommerce.products.controller;

import com.microcommerce.products.dto.request.CreateProductRequest;
import com.microcommerce.products.dto.request.StockUpdateRequest;
import com.microcommerce.products.dto.request.UpdateProductRequest;
import com.microcommerce.products.dto.response.ProductResponse;
import com.microcommerce.products.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration - Produits", description = "API d'administration pour la gestion des produits")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un nouveau produit", 
               description = "Permet aux administrateurs de créer un nouveau produit dans le catalogue")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "409", description = "SKU déjà existant")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        
        log.info("Demande de création de produit par admin: SKU={}", request.getSku());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un produit", 
               description = "Permet aux administrateurs de modifier les informations d'un produit existant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID du produit à modifier", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        
        log.info("Demande de mise à jour de produit par admin: ID={}", id);
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un produit", 
               description = "Permet aux administrateurs de supprimer définitivement un produit du catalogue")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID du produit à supprimer", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de suppression de produit par admin: ID={}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour le stock d'un produit", 
               description = "Permet aux administrateurs de gérer les mouvements de stock (entrées, sorties, ajustements)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock mis à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou stock insuffisant"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponse> updateStock(
            @Parameter(description = "ID du produit", required = true)
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        
        log.info("Demande de mise à jour de stock par admin: ID={}, type={}, quantité={}", 
                id, request.getMovementType(), request.getQuantity());
        ProductResponse response = productService.updateStock(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister tous les produits (admin)", 
               description = "Récupère la liste paginée de tous les produits avec filtres avancés pour les administrateurs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des produits récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis")
    })
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "ID de la catégorie (optionnel)")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Prix minimum (optionnel)")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Prix maximum (optionnel)")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Filtrer par stock disponible (optionnel)")
            @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "Terme de recherche (optionnel)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri (format: propriété,direction)", example = "name,asc")
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        log.info("Demande de liste de produits par admin avec filtres - page: {}, size: {}, sort: {}", page, size, sort);
        Page<ProductResponse> products = productService.findWithFilters(
                categoryId, minPrice, maxPrice, inStock, search, page, size, sort);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un produit par ID (admin)", 
               description = "Récupère les détails complets d'un produit pour les administrateurs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit trouvé"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID du produit", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de détails de produit par admin: ID={}", id);
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un produit par SKU (admin)", 
               description = "Récupère les détails d'un produit en utilisant son code SKU")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit trouvé"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Produit non trouvé")
    })
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Code SKU du produit", required = true)
            @PathVariable String sku) {
        
        log.info("Demande de détails de produit par admin: SKU={}", sku);
        ProductResponse response = productService.getProductBySku(sku);
        return ResponseEntity.ok(response);
    }
}
