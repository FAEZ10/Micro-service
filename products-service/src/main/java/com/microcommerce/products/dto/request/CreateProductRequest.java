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
@Schema(description = "Requête de création d'un produit")
public class CreateProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(min = 2, max = 255, message = "Le nom doit contenir entre 2 et 255 caractères")
    @Schema(description = "Nom du produit", example = "iPhone 15 Pro", required = true)
    private String name;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Description détaillée du produit", 
            example = "Apple iPhone 15 Pro 128GB avec puce A17 Pro et appareil photo professionnel")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Digits(integer = 8, fraction = 2, message = "Le prix doit avoir au maximum 8 chiffres avant la virgule et 2 après")
    @Schema(description = "Prix du produit en euros", example = "1199.99", required = true)
    private BigDecimal price;

    @NotNull(message = "Le stock initial est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    @Schema(description = "Stock initial disponible", example = "50", required = true)
    private Integer stockAvailable;

    @Min(value = 0, message = "Le stock minimum ne peut pas être négatif")
    @Schema(description = "Seuil d'alerte de stock minimum", example = "10")
    @Builder.Default
    private Integer stockMinimum = 0;

    @Schema(description = "ID de la catégorie", example = "1")
    private Long categoryId;

    @NotBlank(message = "Le SKU est obligatoire")
    @Size(min = 3, max = 100, message = "Le SKU doit contenir entre 3 et 100 caractères")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Le SKU ne peut contenir que des lettres majuscules, chiffres et tirets")
    @Schema(description = "Code SKU unique du produit", example = "APPLE-IP15P-128", required = true)
    private String sku;

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
    @Builder.Default
    private Boolean active = true;

    @Size(max = 255, message = "Le titre SEO ne peut pas dépasser 255 caractères")
    @Schema(description = "Titre pour le référencement", example = "iPhone 15 Pro - Smartphone Apple 128GB")
    private String metaTitle;

    @Size(max = 500, message = "La description SEO ne peut pas dépasser 500 caractères")
    @Schema(description = "Description pour le référencement", 
            example = "Découvrez l'iPhone 15 Pro avec ses fonctionnalités avancées et son design premium")
    private String metaDescription;

    @Schema(description = "Tags pour la recherche et le référencement", 
            example = "[\"apple\", \"smartphone\", \"ios\", \"premium\"]")
    private List<String> tags;
}
