package com.microcommerce.clients.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Réponse d'erreur standardisée")
public class ErrorResponse {

    @Schema(description = "Code d'erreur", example = "CLIENT_NOT_FOUND")
    private String code;

    @Schema(description = "Message d'erreur principal", example = "Client introuvable")
    private String message;

    @Schema(description = "Messages d'erreur détaillés")
    private List<String> details;

    @Schema(description = "Timestamp de l'erreur")
    private LocalDateTime timestamp;

    @Schema(description = "Chemin de la requête", example = "/api/v1/clients/123")
    private String path;

    @Schema(description = "Code de statut HTTP", example = "404")
    private int status;

    // Constructeurs
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, String path, int status) {
        this(code, message);
        this.path = path;
        this.status = status;
    }

    public ErrorResponse(String code, String message, List<String> details, String path, int status) {
        this(code, message, path, status);
        this.details = details;
    }

    // Getters et Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", path='" + path + '\'' +
                '}';
    }
}
