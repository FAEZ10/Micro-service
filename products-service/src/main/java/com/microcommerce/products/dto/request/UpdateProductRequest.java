package com.microcommerce.products.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de mise à jour d'un produit")
public class UpdateProductRequest {

    @Size(min = 2, max = 255, message = "Le nom doit contenir entre 2 et 255 caractères")
    @Schema(description = "Nom du produit", example = "iPhone 15 Pro")
    private String name;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Description détaillée du produit")
    private String description;

    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 8, fraction = 2, message = "Le prix doit avoir au maximum 8 chiffres avant la virgule et 2 après")
    @Schema(description = "Prix du produit en euros", example = "1199.99")
    private BigDecimal price;

    @Min(value = 0, message = "Le stock minimum ne peut pas être négatif")
    @Schema(description = "Seuil d'alerte de stock minimum", example = "10")
    private Integer stockMinimum;

    @Schema(description = "ID de la catégorie", example = "1")
    private Long categoryId;

    @DecimalMin(value = "0", message = "Le poids ne peut pas être négatif")
    @Digits(integer = 6, fraction = 2, message = "Le poids doit avoir au maximum 6 chiffres avant la virgule et 2 après")
    @Schema(description = "Poids du produit en grammes", example = "187.0")
    private BigDecimal weight;

    @Size(max = 100, message = "Les dimensions ne peuvent pas dépasser 100 caractères")
    @Schema(description = "Dimensions du produit (LxlxH en cm)", example = "14.7x7.1x0.8")
    private String dimensions;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    @Schema(description = "URL de l'image principale", example = "/images/iphone15pro.jpg")
    private String imageUrl;

    @Schema(description = "Liste des URLs d'images supplémentaires")
    private List<String> imagesUrls;

    @Schema(description = "Produit actif", example = "true")
    private Boolean active;

    @Size(max = 255, message = "Le titre SEO ne peut pas dépasser 255 caractères")
    @Schema(description = "Titre pour le référencement", example = "iPhone 15 Pro - Smartphone Apple 128GB")
    private String metaTitle;

    @Size(max = 500, message = "La description SEO ne peut pas dépasser 500 caractères")
    @Schema(description = "Description pour le référencement")
    private String metaDescription;

    @Schema(description = "Tags pour la recherche et le référencement")
    private List<String> tags;
}
