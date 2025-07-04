package com.microcommerce.clients.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microcommerce API - Service Clients")
                        .version("1.0.0")
                        .description("""
                            API REST pour la gestion des clients et de l'authentification dans l'écosystème Microcommerce.
                            
                            ## Fonctionnalités principales
                            
                            ### 🔐 Authentification
                            - Inscription de nouveaux clients
                            - Connexion avec email/mot de passe
                            - Authentification JWT sécurisée
                            - Rafraîchissement des tokens
                            
                            ### 👤 Gestion des clients
                            - Consultation et modification du profil
                            - Gestion des adresses de livraison/facturation
                            - Administration des comptes (pour les admins)
                            
                            ## Comment utiliser cette API
                            
                            1. **Créer un compte** : `POST /api/v1/auth/register`
                            2. **Se connecter** : `POST /api/v1/auth/login`
                            3. **Utiliser le token** : Ajouter `Authorization: Bearer {token}` dans les en-têtes
                            4. **Accéder aux ressources protégées** : Utiliser les endpoints clients
                            
                            ## Codes d'erreur
                            
                            | Code | Description |
                            |------|-------------|
                            | `CLIENT_NOT_FOUND` | Client introuvable |
                            | `EMAIL_ALREADY_EXISTS` | Email déjà utilisé |
                            | `INVALID_CREDENTIALS` | Identifiants incorrects |
                            | `VALIDATION_ERROR` | Erreurs de validation |
                            | `UNAUTHORIZED` | Token manquant ou invalide |
                            
                            ## Environnements
                            
                            - **Local** : http://localhost:8081
                            - **Production** : https://api.microcommerce.com
                            """)
                        .contact(new Contact()
                                .name("Équipe Développement Microcommerce")
                                .email("dev@microcommerce.com")
                                .url("https://microcommerce.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Environnement local"),
                        new Server().url("https://api.microcommerce.com").description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT pour l'authentification. Format: Bearer {token}"))
                        
                        // Réponses d'erreur communes
                        .addResponses("BadRequest", new ApiResponse()
                                .description("Requête invalide - Erreurs de validation")
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                        
                        .addResponses("Unauthorized", new ApiResponse()
                                .description("Non autorisé - Token manquant ou invalide")
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                        
                        .addResponses("Forbidden", new ApiResponse()
                                .description("Accès interdit - Permissions insuffisantes")
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                        
                        .addResponses("NotFound", new ApiResponse()
                                .description("Ressource introuvable")
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                        
                        .addResponses("Conflict", new ApiResponse()
                                .description("Conflit - Ressource déjà existante")
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse")))))
                        
                        .addResponses("InternalServerError", new ApiResponse()
                                .description("Erreur interne du serveur")
                                .content(new Content()
                                        .addMediaType("application/json", 
                                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))))))
                
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
