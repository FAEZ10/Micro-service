package com.microcommerce.products.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Réponse contenant les informations d'une catégorie")
public class CategoryResponse {

    @Schema(description = "Identifiant unique de la catégorie", example = "1")
    private Long id;

    @Schema(description = "Nom de la catégorie", example = "Électronique")
    private String name;

    @Schema(description = "Description de la catégorie", example = "Appareils électroniques et accessoires")
    private String description;

    @Schema(description = "Identifiant de la catégorie parente", example = "null")
    private Long parentId;

    @Schema(description = "Nom de la catégorie parente", example = "null")
    private String parentName;

    @Schema(description = "Liste des sous-catégories")
    private List<CategoryResponse> children;

    @Schema(description = "Nombre de produits dans cette catégorie", example = "25")
    private Long productCount;

    @Schema(description = "Statut actif de la catégorie", example = "true")
    private Boolean active;

    @Schema(description = "Date de création", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière modification", example = "2025-01-01T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Indique si c'est une catégorie racine", example = "true")
    public boolean isRootCategory() {
        return parentId == null;
    }

    @Schema(description = "Indique si la catégorie a des sous-catégories", example = "true")
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    @Schema(description = "Indique si la catégorie contient des produits", example = "true")
    public boolean hasProducts() {
        return productCount != null && productCount > 0;
    }
}
