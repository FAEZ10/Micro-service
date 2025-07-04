package com.microcommerce.clients.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informations d'une adresse")
public class AddressResponse {

    @Schema(description = "Identifiant unique de l'adresse", example = "1")
    private Long id;

    @Schema(description = "Type d'adresse", example = "SHIPPING")
    private String type;

    @Schema(description = "Nom d'affichage du type", example = "Livraison")
    private String typeDisplayName;

    @Schema(description = "Rue et numéro", example = "123 Rue de la Paix")
    private String street;

    @Schema(description = "Ville", example = "Paris")
    private String city;

    @Schema(description = "Code postal", example = "75001")
    private String postalCode;

    @Schema(description = "Pays", example = "France")
    private String country;

    @Schema(description = "Adresse complète formatée", example = "123 Rue de la Paix, 75001 Paris, France")
    private String fullAddress;

    @Schema(description = "Indique si c'est l'adresse principale pour ce type", example = "true")
    private Boolean isPrimary;

    @Schema(description = "Date de création de l'adresse")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour")
    private LocalDateTime updatedAt;

    // Constructeurs
    public AddressResponse() {}

    public AddressResponse(Long id, String type, String street, String city, String postalCode, String country, Boolean isPrimary) {
        this.id = id;
        this.type = type;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
        this.country = country;
        this.isPrimary = isPrimary;
        this.fullAddress = buildFullAddress();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        updateTypeDisplayName();
    }

    public String getTypeDisplayName() {
        return typeDisplayName;
    }

    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
        updateFullAddress();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        updateFullAddress();
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        updateFullAddress();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        updateFullAddress();
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    private void updateFullAddress() {
        this.fullAddress = buildFullAddress();
    }

    private String buildFullAddress() {
        if (street != null && city != null && postalCode != null && country != null) {
            return street + ", " + postalCode + " " + city + ", " + country;
        }
        return null;
    }

    private void updateTypeDisplayName() {
        if ("SHIPPING".equals(type)) {
            this.typeDisplayName = "Livraison";
        } else if ("BILLING".equals(type)) {
            this.typeDisplayName = "Facturation";
        } else {
            this.typeDisplayName = type;
        }
    }

    @Override
    public String toString() {
        return "AddressResponse{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", fullAddress='" + fullAddress + '\'' +
                ", isPrimary=" + isPrimary +
                '}';
    }
}
