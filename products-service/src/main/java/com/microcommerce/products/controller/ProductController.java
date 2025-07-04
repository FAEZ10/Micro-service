package com.microcommerce.products.controller;

import com.microcommerce.products.dto.response.ProductResponse;
import com.microcommerce.products.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Catalogue de produits", description = "API publique pour consulter le catalogue de produits")
public class ProductController {

    @Operation(
        summary = "Lister tous les produits",
        description = """
            Récupère la liste paginée de tous les produits actifs du catalogue.
            
            **Accès :** Public (aucune authentification requise)
            
            **Paramètres de pagination :**
            - `page` : Numéro de page (commence à 0)
            - `size` : Nombre d'éléments par page (max 100)
            - `sort` : Tri par propriété avec direction (ex: name,asc)
            
            **Propriétés de tri disponibles :**
            - `id` : ID du produit
            - `name` : Nom du produit
            - `price` : Prix
            - `stockAvailable` : Stock disponible
            - `createdAt` : Date de création
            - `category.name` : Nom de catégorie
            
            **Exemples d'utilisation :**
            - Tri par nom : `sort=name,asc`
            - Tri par prix : `sort=price,desc`
            - Multi-tri : `sort=category.name,asc&sort=name,asc`
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liste des produits récupérée avec succès",
            content = @Content(
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "Exemple de réponse",
                    description = "Page de produits avec pagination",
                    value = """
                    {
                      "content": [
                        {
                          "id": 1,
                          "name": "iPhone 15 Pro",
                          "description": "Apple iPhone 15 Pro 128GB Smartphone",
                          "price": 1199.99,
                          "stockAvailable": 50,
                          "stockReserved": 5,
                          "stockMinimum": 10,
                          "totalStock": 55,
                          "sku": "APPLE-IP15P-128",
                          "weight": 187.0,
                          "imageUrl": "/images/iphone15pro.jpg",
                          "active": true,
                          "stockStatus": "IN_STOCK",
                          "inStock": true,
                          "lowStock": false
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
                      "message": "Propriété de tri invalide. Utilisez des propriétés valides comme: id, name, price, stockAvailable, createdAt",
                      "timestamp": "2025-01-01T10:00:00",
                      "path": "/api/v1/products",
                      "status": 400
                    }
                    """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri (format: propriété,direction)", example = "name,asc")
            @RequestParam(defaultValue = "id,asc") String sort) {
        
        log.info("Récupération de la liste des produits - Page: {}, Taille: {}, Sort: {}", 
                page, size, sort);
        
        // Données de test pour le moment
        List<ProductResponse> products = createTestProducts();
        
        // Créer le Pageable à partir des paramètres
        Pageable pageable = PageRequest.of(page, size);
        
        // Simulation de la pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), products.size());
        List<ProductResponse> pageContent = products.subList(start, end);
        
        Page<ProductResponse> productPage = new PageImpl<>(pageContent, pageable, products.size());
        
        log.info("Liste des produits récupérée avec succès - {} produits trouvés", 
                productPage.getTotalElements());
        
        return ResponseEntity.ok(productPage);
    }

    @Operation(
        summary = "Obtenir un produit par ID",
        description = """
            Récupère les informations détaillées d'un produit spécifique par son ID.
            
            **Accès :** Public (aucune authentification requise)
            
            **Utilisation :**
            - Affichage de la fiche produit
            - Vérification de disponibilité
            - Consultation des détails techniques
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Produit trouvé",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produit non trouvé",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Produit non trouvé",
                    value = """
                    {
                      "code": "PRODUCT_NOT_FOUND",
                      "message": "Le produit avec l'ID 999 n'existe pas",
                      "timestamp": "2025-01-01T10:00:00",
                      "path": "/api/v1/products/999",
                      "status": 404
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID du produit", required = true, example = "1")
            @PathVariable Long id) {
        
        log.info("Récupération du produit avec l'ID: {}", id);
        
        // Données de test
        List<ProductResponse> products = createTestProducts();
        ProductResponse product = products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (product == null) {
            log.warn("Produit non trouvé avec l'ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(product);
    }

    // Méthode utilitaire pour créer des données de test
    private List<ProductResponse> createTestProducts() {
        return Arrays.asList(
            ProductResponse.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .description("Apple iPhone 15 Pro 128GB Smartphone")
                .price(new BigDecimal("1199.99"))
                .stockAvailable(50)
                .stockReserved(5)
                .stockMinimum(10)
                .totalStock(55)
                .sku("APPLE-IP15P-128")
                .weight(new BigDecimal("187.0"))
                .dimensions("14.7x7.1x0.8")
                .imageUrl("/images/iphone15pro.jpg")
                .imagesUrls(Arrays.asList("/images/iphone15pro-1.jpg", "/images/iphone15pro-2.jpg"))
                .active(true)
                .metaTitle("iPhone 15 Pro - Smartphone Apple 128GB")
                .metaDescription("Découvrez l'iPhone 15 Pro avec ses fonctionnalités avancées")
                .tags(Arrays.asList("apple", "smartphone", "ios"))
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build(),
            
            ProductResponse.builder()
                .id(2L)
                .name("Samsung Galaxy S24")
                .description("Samsung Galaxy S24 256GB Smartphone")
                .price(new BigDecimal("899.99"))
                .stockAvailable(30)
                .stockReserved(3)
                .stockMinimum(5)
                .totalStock(33)
                .sku("SAMSUNG-GS24-256")
                .weight(new BigDecimal("168.0"))
                .dimensions("14.7x7.0x0.8")
                .imageUrl("/images/galaxys24.jpg")
                .imagesUrls(Arrays.asList("/images/galaxys24-1.jpg", "/images/galaxys24-2.jpg"))
                .active(true)
                .metaTitle("Samsung Galaxy S24 - Smartphone Android 256GB")
                .metaDescription("Le nouveau Samsung Galaxy S24 avec ses innovations")
                .tags(Arrays.asList("samsung", "smartphone", "android"))
                .createdAt(LocalDateTime.now().minusDays(25))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build(),
            
            ProductResponse.builder()
                .id(3L)
                .name("MacBook Air M2")
                .description("Apple MacBook Air M2 13\" Laptop")
                .price(new BigDecimal("1299.99"))
                .stockAvailable(25)
                .stockReserved(2)
                .stockMinimum(5)
                .totalStock(27)
                .sku("APPLE-MBA-M2-13")
                .weight(new BigDecimal("1240.0"))
                .dimensions("30.4x21.2x1.1")
                .imageUrl("/images/macbookair.jpg")
                .imagesUrls(Arrays.asList("/images/macbookair-1.jpg", "/images/macbookair-2.jpg"))
                .active(true)
                .metaTitle("MacBook Air M2 - Ordinateur portable Apple 13 pouces")
                .metaDescription("Le MacBook Air M2 ultra-fin et performant")
                .tags(Arrays.asList("apple", "laptop", "macos"))
                .createdAt(LocalDateTime.now().minusDays(20))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build()
        );
    }
}
