package com.microcommerce.products.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microcommerce API - Service Produits")
                        .version("1.0.0")
                        .description("""
                            API REST pour la gestion du catalogue de produits et des stocks.
                            
                            **Fonctionnalités principales :**
                            - Gestion du catalogue de produits
                            - Organisation par catégories hiérarchiques
                            - Gestion avancée des stocks (disponible, réservé, historique)
                            - Recherche et filtrage des produits
                            - Gestion des images et métadonnées SEO
                            - Suivi des mouvements de stock
                            
                            **Authentification :**
                            - Token JWT requis pour les opérations d'administration
                            - Accès public pour la consultation du catalogue
                            
                            **Gestion des stocks :**
                            - Stock disponible : Quantité vendable
                            - Stock réservé : Quantité en cours de commande
                            - Historique complet des mouvements
                            - Alertes de stock faible
                            
                            **Pour tester l'API :**
                            1. Consulter le catalogue sans authentification
                            2. Utiliser un token JWT admin pour les opérations de gestion
                            3. Suivre les exemples fournis dans chaque endpoint
                            """)
                        .contact(new Contact()
                                .name("Équipe Développement Microcommerce")
                                .email("dev@microcommerce.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Environnement local"),
                        new Server().url("https://api.microcommerce.com").description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT pour l'authentification. Format: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
