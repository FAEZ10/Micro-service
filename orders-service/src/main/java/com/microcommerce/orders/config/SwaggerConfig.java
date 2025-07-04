package com.microcommerce.orders.config;

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
                        .title("Microcommerce API - Service Commandes")
                        .version("1.0.0")
                        .description("""
                            API REST pour la gestion des commandes et du panier d'achat.
                            
                            **Fonctionnalités principales :**
                            - Gestion du panier d'achat (ajout, suppression, modification)
                            - Création et validation des commandes
                            - Suivi des commandes et historique
                            - Gestion des statuts de commande et de paiement
                            
                            **Workflow typique :**
                            1. Récupérer/créer un panier avec GET /api/v1/orders/cart
                            2. Ajouter des produits avec POST /api/v1/orders/cart/add
                            3. Valider la commande avec POST /api/v1/orders/{id}/validate
                            4. Suivre l'évolution avec GET /api/v1/orders/my-orders
                            
                            **Authentification requise :**
                            Toutes les opérations nécessitent un token JWT valide.
                            """)
                        .contact(new Contact()
                                .name("Équipe Développement Microcommerce")
                                .email("dev@microcommerce.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Environnement local"),
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
