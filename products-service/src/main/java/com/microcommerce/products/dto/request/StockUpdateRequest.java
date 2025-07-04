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
@Schema(description = "Requête de mise à jour du stock")
public class StockUpdateRequest {

    @NotNull(message = "La quantité est obligatoire")
    @Schema(description = "Quantité à ajouter ou retirer (positive pour ajout, négative pour retrait)", 
            example = "10", required = true)
    private Integer quantity;

    @NotBlank(message = "Le type de mouvement est obligatoire")
    @Pattern(regexp = "^(INBOUND|OUTBOUND|ADJUSTMENT|RESERVATION|RELEASE)$", 
             message = "Type de mouvement invalide. Valeurs autorisées: INBOUND, OUTBOUND, ADJUSTMENT, RESERVATION, RELEASE")
    @Schema(description = "Type de mouvement de stock", 
            example = "INBOUND", 
            allowableValues = {"INBOUND", "OUTBOUND", "ADJUSTMENT", "RESERVATION", "RELEASE"},
            required = true)
    private String movementType;

    @Size(max = 255, message = "La raison ne peut pas dépasser 255 caractères")
    @Schema(description = "Raison du mouvement de stock", 
            example = "Réapprovisionnement fournisseur")
    private String reason;

    @Size(max = 100, message = "La référence externe ne peut pas dépasser 100 caractères")
    @Schema(description = "Référence externe (numéro de commande, bon de livraison, etc.)", 
            example = "BL-2024-001")
    private String externalReference;
}
