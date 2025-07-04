package com.microcommerce.clients.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Informations d'un client")
public class ClientResponse {

    @Schema(description = "Identifiant unique du client", example = "1")
    private Long id;

    @Schema(description = "Adresse email du client", example = "client@example.com")
    private String email;

    @Schema(description = "Prénom du client", example = "Jean")
    private String firstName;

    @Schema(description = "Nom du client", example = "Dupont")
    private String lastName;

    @Schema(description = "Nom complet du client", example = "Jean Dupont")
    private String fullName;

    @Schema(description = "Numéro de téléphone du client", example = "0123456789")
    private String phone;

    @Schema(description = "Rôle du client", example = "CLIENT")
    private String role;

    @Schema(description = "Statut actif du client", example = "true")
    private Boolean active;

    @Schema(description = "Date de création du compte")
    private LocalDateTime createdAt;

    @Schema(description = "Date de dernière mise à jour")
    private LocalDateTime updatedAt;

    @Schema(description = "Liste des adresses du client")
    private List<AddressResponse> addresses;

    // Constructeurs
    public ClientResponse() {}

    public ClientResponse(Long id, String email, String firstName, String lastName, String phone, String role, Boolean active) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public List<AddressResponse> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressResponse> addresses) {
        this.addresses = addresses;
    }

    // Méthodes utilitaires
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }

    @Override
    public String toString() {
        return "ClientResponse{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}
