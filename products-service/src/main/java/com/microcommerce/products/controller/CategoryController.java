package com.microcommerce.products.controller;

import com.microcommerce.products.dto.response.CategoryResponse;
import com.microcommerce.products.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Catégories", description = "API publique pour la consultation des catégories de produits")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lister toutes les catégories", 
               description = "Récupère la liste paginée de toutes les catégories actives")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès")
    })
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri (format: propriété,direction)", example = "name,asc")
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        log.info("Demande de liste de catégories publique - page: {}, size: {}, sort: {}", page, size, sort);
        Page<CategoryResponse> categories = categoryService.getAllCategories(page, size, sort);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une catégorie par ID", 
               description = "Récupère les détails d'une catégorie spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie trouvée"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de détails de catégorie publique: ID={}", id);
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/root")
    @Operation(summary = "Récupérer les catégories racines", 
               description = "Récupère toutes les catégories de niveau racine (sans parent)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégories racines récupérées avec succès")
    })
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        
        log.info("Demande de catégories racines publique");
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Récupérer les sous-catégories", 
               description = "Récupère toutes les sous-catégories d'une catégorie donnée")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sous-catégories récupérées avec succès"),
        @ApiResponse(responseCode = "404", description = "Catégorie parent non trouvée")
    })
    public ResponseEntity<List<CategoryResponse>> getSubCategories(
            @Parameter(description = "ID de la catégorie parent", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de sous-catégories publique: parent ID={}", id);
        List<CategoryResponse> categories = categoryService.getSubCategories(id);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Récupérer la hiérarchie d'une catégorie", 
               description = "Récupère la hiérarchie complète d'une catégorie (parents et enfants)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hiérarchie récupérée avec succès"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    public ResponseEntity<List<CategoryResponse>> getCategoryHierarchy(
            @Parameter(description = "ID de la catégorie", required = true)
            @PathVariable Long id) {
        
        log.info("Demande de hiérarchie de catégorie publique: ID={}", id);
        List<CategoryResponse> hierarchy = categoryService.getCategoryHierarchy(id);
        return ResponseEntity.ok(hierarchy);
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des catégories", 
               description = "Recherche des catégories par nom")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Résultats de recherche récupérés avec succès")
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
        
        log.info("Demande de recherche de catégories publique: terme={}, page={}, size={}, sort={}", name, page, size, sort);
        Page<CategoryResponse> categories = categoryService.searchCategories(name, page, size, sort);
        return ResponseEntity.ok(categories);
    }
}
