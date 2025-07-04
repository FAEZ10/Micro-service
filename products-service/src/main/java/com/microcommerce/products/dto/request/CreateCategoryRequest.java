package com.microcommerce.products.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête de création d'une catégorie")
public class CreateCategoryRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Schema(description = "Nom de la catégorie", example = "Smartphones", required = true)
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Schema(description = "Description de la catégorie", 
            example = "Téléphones intelligents et accessoires")
    private String description;

    @Schema(description = "ID de la catégorie parent (null pour une catégorie racine)", 
            example = "1")
    private Long parentId;

    @Schema(description = "Catégorie active", example = "true")
    @Builder.Default
    private Boolean active = true;
}
