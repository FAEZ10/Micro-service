package com.microcommerce.products.controller;

import com.microcommerce.products.dto.request.CreateCategoryRequest;
import com.microcommerce.products.dto.response.CategoryResponse;
import com.microcommerce.products.service.CategoryService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administration - Catégories", description = "API d'administration pour la gestion des catégories")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une nouvelle catégorie", 
               description = "Permet aux administrateurs de créer une nouvelle catégorie de produits")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie parent non trouvée")
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        
        log.info("Demande de création de catégorie par admin: nom={}", request.getName());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour une catégorie", 
               description = "Permet aux administrateurs de modifier les informations d'une catégorie existante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou référence circulaire"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID de la catégorie à modifier", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        
        log.info("Demande de mise à jour de catégorie par admin: ID={}", id);
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une catégorie", 
               description = "Permet aux administrateurs de supprimer une catégorie (uniquement si elle ne contient pas de produits ou sous-catégories)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Catégorie supprimée avec succès"),
        @ApiResponse(responseCode = "400", description = "Impossible de supprimer - catégorie contient des produits ou sous-catégories"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la catégorie à supprimer", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de suppression de catégorie par admin: ID={}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lister toutes les catégories (admin)", 
               description = "Récupère la liste paginée de toutes les catégories pour les administrateurs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis")
    })
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri (format: propriété,direction)", example = "name,asc")
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        log.info("Demande de liste de catégories par admin - page: {}, size: {}, sort: {}", page, size, sort);
        Page<CategoryResponse> categories = categoryService.getAllCategories(page, size, sort);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer une catégorie par ID (admin)", 
               description = "Récupère les détails complets d'une catégorie pour les administrateurs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie trouvée"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de détails de catégorie par admin: ID={}", id);
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechercher des catégories (admin)", 
               description = "Recherche des catégories par nom pour les administrateurs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis")
    })
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @Parameter(description = "Terme de recherche", required = true)
            @RequestParam String name,
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri (format: propriété,direction)", example = "name,asc")
            @RequestParam(defaultValue = "name,asc") String sort) {
        
        log.info("Demande de recherche de catégories par admin: terme={}, page={}, size={}, sort={}", name, page, size, sort);
        Page<CategoryResponse> categories = categoryService.searchCategories(name, page, size, sort);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/root")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les catégories racines (admin)", 
               description = "Récupère toutes les catégories de niveau racine (sans parent)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégories racines récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis")
    })
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        
        log.info("Demande de catégories racines par admin");
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/subcategories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les sous-catégories (admin)", 
               description = "Récupère toutes les sous-catégories d'une catégorie donnée")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sous-catégories récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie parent non trouvée")
    })
    public ResponseEntity<List<CategoryResponse>> getSubCategories(
            @Parameter(description = "ID de la catégorie parent", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de sous-catégories par admin: parent ID={}", id);
        List<CategoryResponse> categories = categoryService.getSubCategories(id);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/hierarchy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer la hiérarchie d'une catégorie (admin)", 
               description = "Récupère la hiérarchie complète d'une catégorie (parents et enfants)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hiérarchie récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryHierarchy(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de hiérarchie de catégorie par admin: ID={}", id);
        List<CategoryResponse> hierarchy = categoryService.getCategoryHierarchy(id);
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les statistiques d'une catégorie (admin)", 
               description = "Récupère les statistiques d'une catégorie (nombre de produits, sous-catégories)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistiques récupérées avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle administrateur requis"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<CategoryStatsResponse> getCategoryStats(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de statistiques de catégorie par admin: ID={}", id);
        
        long productCount = categoryService.getProductCountInCategory(id);
        long subCategoryCount = categoryService.getSubCategoryCount(id);
        
        CategoryStatsResponse stats = CategoryStatsResponse.builder()
                .categoryId(id)
                .productCount(productCount)
                .subCategoryCount(subCategoryCount)
                .build();
        
        return ResponseEntity.ok(stats);
    }

    public static class CategoryStatsResponse {
        private Long categoryId;
        private Long productCount;
        private Long subCategoryCount;

        public static CategoryStatsResponseBuilder builder() {
            return new CategoryStatsResponseBuilder();
        }

        public static class CategoryStatsResponseBuilder {
            private Long categoryId;
            private Long productCount;
            private Long subCategoryCount;

            public CategoryStatsResponseBuilder categoryId(Long categoryId) {
                this.categoryId = categoryId;
                return this;
            }

            public CategoryStatsResponseBuilder productCount(Long productCount) {
                this.productCount = productCount;
                return this;
            }

            public CategoryStatsResponseBuilder subCategoryCount(Long subCategoryCount) {
                this.subCategoryCount = subCategoryCount;
                return this;
            }

            public CategoryStatsResponse build() {
                CategoryStatsResponse response = new CategoryStatsResponse();
                response.categoryId = this.categoryId;
                response.productCount = this.productCount;
                response.subCategoryCount = this.subCategoryCount;
                return response;
            }
        }

        // Getters
        public Long getCategoryId() { return categoryId; }
        public Long getProductCount() { return productCount; }
        public Long getSubCategoryCount() { return subCategoryCount; }
    }
}
