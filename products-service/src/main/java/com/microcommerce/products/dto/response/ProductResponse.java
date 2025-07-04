package com.microcommerce.products.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant les informations complètes d'un produit")
public class ProductResponse {

    @Schema(description = "Identifiant unique du produit", example = "1")
    private Long id;

    @Schema(description = "Nom du produit", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "Description détaillée du produit", example = "Apple iPhone 15 Pro 128GB Smartphone")
    private String description;

    @Schema(description = "Prix unitaire du produit", example = "1199.99")
    private BigDecimal price;

    @Schema(description = "Stock disponible à la vente", example = "50")
    private Integer stockAvailable;

    @Schema(description = "Stock réservé (commandes en cours)", example = "5")
    private Integer stockReserved;

    @Schema(description = "Stock minimum avant alerte", example = "10")
    private Integer stockMinimum;

    @Schema(description = "Stock total (disponible + réservé)", example = "55")
    private Integer totalStock;

    @Schema(description = "Informations de la catégorie")
    private CategoryResponse category;

    @Schema(description = "Code SKU unique du produit", example = "APPLE-IP15P-128")
    private String sku;

    @Schema(description = "Poids du produit en grammes", example = "187.0")
    private BigDecimal weight;

    @Schema(description = "Dimensions du produit (LxWxH en cm)", example = "14.7x7.1x0.8")
    private String dimensions;

    @Schema(description = "URL de l'image principale", example = "/images/iphone15pro.jpg")
    private String imageUrl;

    @Schema(description = "Liste des URLs d'images supplémentaires")
    private List<String> imagesUrls;

    @Schema(description = "Statut actif du produit", example = "true")
    private Boolean active;

    @Schema(description = "Titre SEO pour les moteurs de recherche", example = "iPhone 15 Pro - Smartphone Apple 128GB")
    private String metaTitle;

    @Schema(description = "Description SEO pour les moteurs de recherche")
    private String metaDescription;

    @Schema(description = "Tags associés au produit pour la recherche")
    private List<String> tags;

    @Schema(description = "Date de création du produit", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification", example = "2025-01-01T10:00:00")
    private LocalDateTime updatedAt;

    // Méthodes utilitaires avec annotations Swagger
    @Schema(description = "Indique si le produit est en stock", example = "true")
    public boolean isInStock() {
        return stockAvailable != null && stockAvailable > 0;
    }

    @Schema(description = "Indique si le stock est faible", example = "false")
    public boolean isLowStock() {
        return stockAvailable != null && stockMinimum != null && stockAvailable <= stockMinimum;
    }

    @Schema(description = "Statut du stock", example = "EN_STOCK")
    public StockStatus getStockStatus() {
        if (!isInStock()) {
            return StockStatus.OUT_OF_STOCK;
        } else if (isLowStock()) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.IN_STOCK;
        }
    }

    @Schema(description = "Statut du stock du produit")
    public enum StockStatus {
        @Schema(description = "En stock")
        IN_STOCK,
        @Schema(description = "Stock faible")
        LOW_STOCK,
        @Schema(description = "Rupture de stock")
        OUT_OF_STOCK
    }
}
