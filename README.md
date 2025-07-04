# Microcommerce - Architecture Microservices

## 🎯 Vue d'ensemble

Architecture microservices complète pour une application de commerce électronique avec des services autonomes exposant des API REST sécurisées et communication asynchrone via Apache Kafka.

## 📋 État du projet

### ✅ Services développés et fonctionnels

#### 1. Service Clients (Port 8081)
- **Statut** : ✅ Complet et testé
- **Fonctionnalités** :
  - Inscription et authentification JWT
  - Gestion des profils clients
  - Gestion des adresses (livraison/facturation)
  - API d'administration
  - Événements Kafka (création/modification clients)
- **Base de données** : PostgreSQL (port 5432)
- **Documentation** : http://localhost:8081/swagger-ui.html

#### 2. Service Produits (Port 8082)
- **Statut** : ✅ Complet et testé
- **Fonctionnalités** :
  - Catalogue de produits avec pagination
  - Gestion des catégories hiérarchiques
  - Gestion avancée des stocks
  - API publique de consultation
  - Événements Kafka (modifications produits/stocks)
- **Base de données** : PostgreSQL (port 5433)
- **Documentation** : http://localhost:8082/swagger-ui.html

#### 3. Service Commandes (Port 8083)
- **Statut** : ✅ Complet et testé
- **Fonctionnalités** :
  - Gestion du panier
  - Processus de commande
  - Intégration avec les autres services
  - Consommation d'événements produits
  - Événements Kafka (commandes)
- **Base de données** : PostgreSQL (port 5434)
- **Documentation** : http://localhost:8083/swagger-ui.html

## 🏗️ Architecture technique

### Stack technologique
- **Langage** : Java 17
- **Framework** : Spring Boot 3.2.1
- **Sécurité** : Spring Security + JWT
- **Base de données** : PostgreSQL (une par service)
- **Message Broker** : Apache Kafka
- **Containerisation** : Docker + Docker Compose
- **Documentation API** : OpenAPI 3 (Swagger)
- **Build** : Maven

### Ports et services
```
┌─────────────────────┬──────┬─────────────────────────────┐
│ Service             │ Port │ Description                 │
├─────────────────────┼──────┼─────────────────────────────┤
│ clients-service     │ 8081 │ Gestion clients & auth      │
│ products-service    │ 8082 │ Catalogue & stocks          │
│ orders-service      │ 8083 │ Commandes & panier          │
│ swagger-ui          │ 8080 │ Documentation centralisée   │
│ kafka-ui            │ 8090 │ Interface Kafka             │
│ kafka               │ 9092 │ Message broker              │
│ postgres-clients    │ 5432 │ DB clients                  │
│ postgres-products   │ 5433 │ DB produits                 │
│ postgres-orders     │ 5434 │ DB commandes                │
└─────────────────────┴──────┴─────────────────────────────┘
```

## 🚀 Démarrage rapide

### Prérequis
- Docker et Docker Compose
- Java 17+ (pour développement local)
- Maven 3.9+ (pour développement local)

### 1. Démarrer l'infrastructure complète
```bash
# Démarrer tous les services
docker compose up -d

# Ou démarrer progressivement
docker compose up -d postgres-clients postgres-products postgres-orders kafka zookeeper
docker compose up -d clients-service products-service orders-service
docker compose up -d swagger-ui kafka-ui
```

### 2. Vérifier le démarrage
```bash
# Vérifier l'état des services
docker compose ps

# Voir les logs
docker compose logs -f clients-service
docker compose logs -f products-service
docker compose logs -f orders-service
```

## 📚 Documentation API

### Accès aux documentations
- **Interface centralisée** : http://localhost:8080
- **Service Clients** : http://localhost:8081/swagger-ui.html
- **Service Produits** : http://localhost:8082/swagger-ui.html
- **Service Commandes** : http://localhost:8083/swagger-ui.html
- **Interface Kafka** : http://localhost:8090

### Routes API complètes

#### 🔐 Service Clients (Port 8081)

##### Authentification (Public)
```
POST   /api/v1/auth/register     # Inscription
POST   /api/v1/auth/login        # Connexion
POST   /api/v1/auth/refresh      # Renouveler token
```

##### Gestion Client (Authentifié)
```
GET    /api/v1/clients/profile   # Profil client
PUT    /api/v1/clients/profile   # Modifier profil
DELETE /api/v1/clients/profile   # Supprimer compte
POST   /api/v1/clients/addresses # Ajouter adresse
GET    /api/v1/clients/addresses # Lister adresses
PUT    /api/v1/clients/addresses/{id} # Modifier adresse
DELETE /api/v1/clients/addresses/{id} # Supprimer adresse
```

##### Administration (Admin uniquement)
```
GET    /api/v1/admin/clients     # Liste clients
GET    /api/v1/admin/clients/{id} # Détail client
PUT    /api/v1/admin/clients/{id} # Modifier client
DELETE /api/v1/admin/clients/{id} # Supprimer client
GET    /api/v1/admin/clients/{id}/addresses # Adresses client
```

#### 📦 Service Produits (Port 8082)

##### Consultation (Public)
```
GET    /api/v1/products          # Liste produits (pagination)
GET    /api/v1/products/{id}     # Détail produit
GET    /api/v1/products/search   # Recherche produits
GET    /api/v1/categories        # Liste catégories
GET    /api/v1/categories/{id}   # Détail catégorie
GET    /api/v1/categories/{id}/products # Produits par catégorie
```

##### Administration Produits (Admin uniquement)
```
POST   /api/v1/admin/products    # Créer produit
PUT    /api/v1/admin/products/{id} # Modifier produit
DELETE /api/v1/admin/products/{id} # Supprimer produit
PUT    /api/v1/admin/products/{id}/stock # Gérer stock
GET    /api/v1/admin/products/{id}/stock-history # Historique stock
```

##### Administration Catégories (Admin uniquement)
```
POST   /api/v1/admin/categories  # Créer catégorie
PUT    /api/v1/admin/categories/{id} # Modifier catégorie
DELETE /api/v1/admin/categories/{id} # Supprimer catégorie
```

#### 🛒 Service Commandes (Port 8083)

##### Gestion Panier (Authentifié)
```
GET    /api/v1/orders/cart       # Récupérer panier
POST   /api/v1/orders/cart/add   # Ajouter au panier
DELETE /api/v1/orders/cart/remove/{productId} # Supprimer du panier
```

##### Gestion Commandes (Authentifié)
```
GET    /api/v1/orders/{orderId}  # Détail commande
GET    /api/v1/orders/my-orders  # Mes commandes
POST   /api/v1/orders/{orderId}/validate # Valider commande
```

## 🧪 Tests et validation

### Tester l'API Clients
```bash
# Inscription d'un nouveau client
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Jean",
    "lastName": "Dupont"
  }'

# Connexion
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Tester l'API Produits
```bash
# Lister les produits
curl http://localhost:8082/api/v1/products

# Obtenir un produit spécifique
curl http://localhost:8082/api/v1/products/1

# Avec pagination et tri
curl "http://localhost:8082/api/v1/products?page=0&size=10&sort=name,asc"
```

### Tester l'API Commandes
```bash
# Récupérer le panier (nécessite un token JWT)
curl -X GET http://localhost:8083/api/v1/orders/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Ajouter un produit au panier
curl -X POST http://localhost:8083/api/v1/orders/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

## 📨 Apache Kafka - Communication asynchrone

### Topics Kafka configurés
- **client-events** : Événements clients (création, modification)
- **product-events** : Événements produits (création, modification, stock)
- **order-events** : Événements commandes (validation, statut)

### Interface Kafka UI
- **URL** : http://localhost:8090
- **Fonctionnalités** :
  - Visualisation des topics
  - Monitoring des messages
  - Gestion des consumers
  - Métriques en temps réel

### Tester Kafka

#### 1. Via l'interface web
Accédez à http://localhost:8090 pour voir :
- Liste des topics
- Messages en temps réel
- Consumers actifs
- Métriques de performance

#### 2. Via ligne de commande
```bash
# Lister les topics
docker exec kafka kafka-topics --bootstrap-server kafka:9092 --list

# Créer un topic manuellement
docker exec kafka kafka-topics --bootstrap-server kafka:9092 \
  --create --topic test-topic --partitions 1 --replication-factor 1

# Consommer des messages
docker exec kafka kafka-console-consumer \
  --bootstrap-server kafka:9092 --topic client-events --from-beginning

# Produire des messages
docker exec -it kafka kafka-console-producer \
  --bootstrap-server kafka:9092 --topic client-events
```

#### 3. Déclencher des événements
```bash
# Créer un client (génère un événement client-events)
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "kafka-test@example.com", "password": "password123", "firstName": "Kafka", "lastName": "Test"}'

# Modifier un stock (génère un événement product-events)
curl -X PUT http://localhost:8082/api/v1/admin/products/1/stock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -d '{"quantity": 100, "operation": "ADD", "reason": "Réapprovisionnement"}'
```

## 🗄️ Base de données

### Connexions directes
```bash
# Base clients
docker exec -it postgres-clients psql -U postgres -d microcommerce_clients

# Base produits
docker exec -it postgres-products psql -U postgres -d microcommerce_products

# Base commandes
docker exec -it postgres-orders psql -U postgres -d microcommerce_orders
```

### Données de test
Les bases de données sont initialisées avec des données de test :
- **Clients** : 3 utilisateurs (1 admin, 2 clients)
- **Produits** : 5 produits dans différentes catégories
- **Commandes** : 2 commandes d'exemple

## 🔧 Développement

### Structure du projet
```
microcommerce/
├── docker-compose.yml              # Orchestration des services
├── infrastructure/
│   └── postgres/                   # Scripts d'initialisation DB
├── clients-service/                # ✅ Service clients complet
│   ├── src/main/java/com/microcommerce/clients/
│   ├── Dockerfile
│   └── pom.xml
├── products-service/               # ✅ Service produits complet
│   ├── src/main/java/com/microcommerce/products/
│   ├── Dockerfile
│   └── pom.xml
└── orders-service/                 # ✅ Service commandes complet
    ├── src/main/java/com/microcommerce/orders/
    ├── Dockerfile
    └── pom.xml
```

### Commandes utiles
```bash
# Rebuild un service
docker compose build orders-service
docker compose up -d orders-service

# Voir les logs en temps réel
docker compose logs -f

# Arrêter tous les services
docker compose down

# Nettoyer complètement (attention : supprime les données)
docker compose down -v

# Démarrer seulement Kafka UI
docker compose up -d kafka-ui
```

## 🔐 Sécurité

### Authentification JWT
- **Secret** : Configurable via variable d'environnement `JWT_SECRET`
- **Expiration** : 15 minutes (configurable)
- **Refresh** : 7 jours (configurable)

### Rôles utilisateurs
- **CLIENT** : Accès aux fonctionnalités client
- **ADMIN** : Accès complet à l'administration

### Comptes de test
```
Admin : admin@microcommerce.com / admin123
Client : client1@microcommerce.com / password123
Client : client2@microcommerce.com / password123
```

## 📊 Monitoring

### Health checks
```bash
# Vérifier la santé des services
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

### Métriques
```bash
# Métriques des services
curl http://localhost:8081/actuator/metrics
curl http://localhost:8082/actuator/metrics
curl http://localhost:8083/actuator/metrics
```

### Kafka Monitoring
- **Interface web** : http://localhost:8090
- **Métriques** : Topics, partitions, consumers, lag
- **Messages** : Visualisation en temps réel

## 🚀 Architecture Event-Driven

### Communication inter-services
1. **Synchrone** : API REST pour les opérations immédiates
2. **Asynchrone** : Kafka pour les événements et notifications

### Flux d'événements
```
Client créé → client-events → Notification services
Produit modifié → product-events → Cache invalidation
Stock mis à jour → product-events → Orders service
Commande validée → order-events → Inventory reservation
```

### Patterns implémentés
- **Event Sourcing** : Historique des modifications
- **CQRS** : Séparation lecture/écriture
- **Saga Pattern** : Transactions distribuées
- **Outbox Pattern** : Garantie de livraison

## 🤝 Contribution

### Standards de développement
- **Code** : 100% en anglais
- **Documentation** : En français
- **Tests** : Obligatoires pour les nouvelles fonctionnalités
- **API** : Documentation Swagger complète
- **Kafka** : Tous les événements documentés

### Workflow
1. Développer un service à la fois
2. Tester complètement avant intégration
3. Documenter les APIs et événements
4. Valider avec Docker Compose
5. Tester les flux Kafka

## 📞 Support

Pour toute question ou problème :
- Consulter la documentation Swagger
- Vérifier les logs des services
- Utiliser l'interface Kafka UI pour déboguer
- Tester les endpoints avec les exemples fournis

## 🎉 Fonctionnalités avancées

### Implémentées
- ✅ Architecture microservices complète
- ✅ Authentification JWT sécurisée
- ✅ Communication Kafka asynchrone
- ✅ Base de données par service
- ✅ Documentation API complète
- ✅ Interface de monitoring Kafka
- ✅ Gestion des stocks en temps réel
- ✅ Panier et commandes

### Prochaines étapes
- 🔄 Gestion des paiements
- 🔄 Notifications push
- 🔄 Analytics et rapports
- 🔄 Cache distribué (Redis)
- 🔄 Service de livraison
- 🔄 API Gateway

