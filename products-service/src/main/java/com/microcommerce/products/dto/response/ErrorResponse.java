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
@Schema(description = "Réponse d'erreur standardisée")
public class ErrorResponse {

    @Schema(description = "Code d'erreur unique", example = "PRODUCT_NOT_FOUND")
    private String code;

    @Schema(description = "Message d'erreur principal", example = "Le produit demandé n'existe pas")
    private String message;

    @Schema(description = "Liste des détails d'erreur (pour les erreurs de validation)")
    private List<String> details;

    @Schema(description = "Chemin de l'endpoint qui a généré l'erreur", example = "/api/v1/products/999")
    private String path;

    @Schema(description = "Code de statut HTTP", example = "404")
    private Integer status;

    @Schema(description = "Timestamp de l'erreur", example = "2025-01-01T10:00:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructeur pour erreurs simples
    public ErrorResponse(String code, String message, String path, Integer status) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    // Constructeur pour erreurs avec détails
    public ErrorResponse(String code, String message, List<String> details, String path, Integer status) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.path = path;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
