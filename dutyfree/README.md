# 🛍️ Duty Free Management System - Backend

Système de gestion complet pour boutique Duty Free à l'Aéroport International de Ouagadougou.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)
![License](https://img.shields.io/badge/License-Proprietary-yellow)

## 📋 Table des Matières

- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Configuration](#configuration)
- [Démarrage](#démarrage)
- [API Documentation](#api-documentation)
- [Base de Données](#base-de-données)
- [Tests](#tests)
- [Déploiement](#déploiement)
- [Sécurité](#sécurité)
- [Contribution](#contribution)
- [Support](#support)
- [License](#license)

## 🎯 Aperçu

Application backend Spring Boot pour la gestion complète d'une boutique Duty Free, incluant :

- Gestion des ventes en temps réel
- Gestion des stocks et inventaire
- Système de caisse multi-points de vente
- Gestion des sommiers douaniers
- Programme de fidélité clients
- Système de promotions
- Reporting et analytics
- Paiements multi-devises (XOF, EUR, USD)

## ✨ Fonctionnalités

### 🛒 Gestion des Ventes
- ✅ Enregistrement des ventes en temps réel
- ✅ Support multi-caisses
- ✅ Gestion des paiements (espèces, carte, mobile money)
- ✅ Paiements multi-devises avec conversion automatique
- ✅ Génération automatique de tickets de caisse
- ✅ Informations passagers (carte d'embarquement)

### 📦 Gestion des Stocks
- ✅ Suivi en temps réel des stocks
- ✅ Gestion des entrées/sorties
- ✅ Alertes stock bas
- ✅ Gestion des lots et dates de péremption
- ✅ Inventaire multi-emplacements

### 🏛️ Gestion Douanière
- ✅ Gestion des sommiers (entreposage fictif)
- ✅ Suivi de l'apurement des sommiers
- ✅ Alertes réglementaires
- ✅ Traçabilité complète

### 🎁 Programme de Fidélité
- ✅ Cartes de fidélité
- ✅ Système de points
- ✅ Porte-monnaie électronique
- ✅ Tiers (Standard, Silver, Gold, Platinum)
- ✅ Remises automatiques

### 📊 Reporting & Analytics
- ✅ Rapports de ventes (journalier, hebdomadaire, mensuel)
- ✅ Performance par caissier
- ✅ Performance par point de vente
- ✅ Analyse des ventes par catégorie/produit
- ✅ Export Excel et PDF

### 👥 Gestion des Utilisateurs
- ✅ Authentification JWT
- ✅ Gestion des rôles (Admin, Superviseur, Caissier, Stock Manager)
- ✅ Audit trail complet
- ✅ Permissions granulaires

## 🏗️ Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React/Angular)                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTPS/REST
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         Nginx (Reverse Proxy)                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Spring Boot Application                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Controllers  │  │   Services   │  │ Repositories │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Security   │  │  WebSocket   │  │    Cache     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                    │                    │
                    ▼                    ▼
         ┌──────────────────┐  ┌──────────────────┐
         │   PostgreSQL     │  │      Redis       │
         │   (Database)     │  │     (Cache)      │
         └──────────────────┘  └──────────────────┘
```

## 🛠️ Technologies

### Backend
- **Java 17** - Langage de programmation
- **Spring Boot 3.2.0** - Framework principal
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - ORM et accès aux données
- **PostgreSQL 15** - Base de données relationnelle
- **Redis 7** - Cache et sessions
- **Flyway** - Migration de base de données
- **JWT** - Tokens d'authentification

### Outils
- **Maven** - Gestion de dépendances
- **Lombok** - Réduction du code boilerplate
- **MapStruct** - Mapping d'objets
- **Swagger/OpenAPI** - Documentation API
- **Docker** - Conteneurisation
- **Nginx** - Reverse proxy

### Bibliothèques
- **iText** - Génération de PDF
- **Apache POI** - Génération d'Excel
- **Jackson** - Sérialisation JSON

## 📋 Prérequis

### Développement Local
- Java 17 ou supérieur
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Avec Docker
- Docker 20.10+
- Docker Compose 2.0+

## 🚀 Installation

### 1. Cloner le Repository
```bash
git clone https://github.com/votre-org/duty-free-backend.git
cd duty-free-backend
```

### 2. Configuration de la Base de Données

#### Option A: Installation Locale
```bash
# Créer la base de données
createdb dutyfree_db

# Ou avec psql
psql -U postgres
CREATE DATABASE dutyfree_db;
\q
```

#### Option B: Avec Docker
```bash
docker-compose up -d postgres redis
```

### 3. Configuration de l'Application

Copier le fichier de configuration :
```bash
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

Modifier les paramètres selon votre environnement.

### 4. Installer les Dépendances
```bash
mvn clean install
```

## ⚙️ Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dutyfree_db
    username: postgres
    password: votre_mot_de_passe
  
  redis:
    host: localhost
    port: 6379

jwt:
  secret: votre_secret_jwt_tres_long
  expiration: 86400000
```

### Variables d'Environnement

Créer un fichier `.env` :
```bash
cp .env.example .env
```

Modifier les valeurs :
```env
DB_NAME=dutyfree_db
DB_USER=postgres
DB_PASSWORD=votre_mot_de_passe
JWT_SECRET=votre_secret_jwt
REDIS_PASSWORD=votre_redis_password
```

## 🏃 Démarrage

### Mode Développement

#### Sans Docker
```bash
mvn spring-boot:run
```

#### Avec Docker
```bash
docker-compose up
```

Ou avec le Makefile :
```bash
make dev
```

### Mode Production
```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/duty-free-backend-1.0.0.jar

# Ou avec Docker
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Ou avec le Makefile :
```bash
make prod-build
```

L'application sera accessible sur : **http://localhost:8080**

## 📚 API Documentation

### Swagger UI

Une fois l'application démarrée, accéder à la documentation interactive :
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI Specification
```
http://localhost:8080/api-docs
```

### Endpoints Principaux

#### Authentification
```
POST   /api/auth/login          - Connexion utilisateur
GET    /api/auth/me             - Informations utilisateur connecté
POST   /api/auth/logout         - Déconnexion
```

#### Produits
```
GET    /api/products            - Liste des produits
POST   /api/products            - Créer un produit
GET    /api/products/{id}       - Détails d'un produit
PUT    /api/products/{id}       - Modifier un produit
DELETE /api/products/{id}       - Supprimer un produit
GET    /api/products/search     - Rechercher des produits
```

#### Ventes
```
POST   /api/sales               - Créer une vente
GET    /api/sales/{id}          - Détails d'une vente
POST   /api/sales/{id}/complete - Finaliser une vente
POST   /api/sales/{id}/cancel   - Annuler une vente
```

#### Stocks
```
GET    /api/stocks/product/{id} - Stock d'un produit
POST   /api/stocks              - Ajouter du stock
PUT    /api/stocks/{id}/adjust  - Ajuster le stock
```

#### Rapports
```
GET    /api/reports/sales       - Rapport de ventes
GET    /api/reports/cashier/{id} - Rapport caissier
GET    /api/reports/daily       - Rapport journalier
```

### Exemples d'Utilisation

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Réponse :
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "admin",
    "fullName": "System Administrator",
    "role": "ADMIN"
  }
}
```

#### Créer une Vente
```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cashRegisterId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "payments": [
      {
        "paymentMethod": "CASH",
        "currency": "XOF",
        "amount": 30000
      }
    ]
  }'
```

## 🗄️ Base de Données

### Schéma

Le schéma complet est géré par Flyway. Les migrations sont dans :
```
src/main/resources/db/migration/
├── V1__init_schema.sql          # Création des tables
├── V2__insert_default_data.sql  # Données par défaut
├── V3__add_indexes.sql          # Index de performance
└── V4__add_triggers.sql         # Triggers
```

### Utilisateurs par Défaut

| Username    | Password   | Role          |
|-------------|------------|---------------|
| admin       | admin123   | ADMIN         |
| superviseur | super123   | SUPERVISEUR   |
| caissier    | caisse123  | CAISSIER      |
| stockmgr    | stock123   | STOCK_MANAGER |

⚠️ **IMPORTANT** : Changer ces mots de passe en production !

### Commandes Utiles
```bash
# Appliquer les migrations
make db-migrate

# Voir le statut des migrations
make db-info

# Backup de la base de données
make backup-db

# Restaurer une sauvegarde
make restore-db FILE=backup_20231215.sql

# Se connecter à la base de données
make shell-db
```

## 🧪 Tests

### Exécuter tous les tests
```bash
mvn test
```

### Tests par catégorie
```bash
# Tests unitaires
mvn test -Dtest=*Test

# Tests d'intégration
mvn test -Dtest=*IT

# Avec couverture
mvn clean test jacoco:report
```

### Rapport de couverture

Après exécution des tests :
```
target/site/jacoco/index.html
```

## 🚢 Déploiement

### Docker Compose (Recommandé)
```bash
# Build et démarrage
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build

# Vérifier les logs
docker-compose logs -f app

# Vérifier le statut
docker-compose ps
```

### Avec Makefile
```bash
# Déploiement production
make prod-build

# Vérifier les logs
make logs-app

# Vérifier la santé de l'application
make health
```

### Variables d'Environnement Production

Créer un fichier `.env` en production avec des valeurs sécurisées :
```env
DB_PASSWORD=mot_de_passe_tres_securise
JWT_SECRET=secret_jwt_de_256_bits_minimum
REDIS_PASSWORD=redis_password_securise
```

### Reverse Proxy Nginx

Le fichier `nginx.conf` est configuré avec :
- HTTPS/SSL
- Compression Gzip
- Rate limiting
- Security headers
- WebSocket support

Pour générer des certificats SSL auto-signés (développement) :
```bash
mkdir -p ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ssl/key.pem -out ssl/cert.pem
```

Pour production, utilisez Let's Encrypt :
```bash
certbot certonly --standalone -d votredomaine.com
```

## 🔒 Sécurité

### Authentification

- JWT tokens avec expiration
- BCrypt pour le hachage des mots de passe
- Refresh tokens (à implémenter si nécessaire)

### Autorisation

- Contrôle d'accès basé sur les rôles (RBAC)
- Permissions granulaires par endpoint
- Audit trail complet

### Best Practices

- ✅ Pas de secrets dans le code source
- ✅ Variables d'environnement pour la configuration sensible
- ✅ HTTPS obligatoire en production
- ✅ Rate limiting sur les endpoints critiques
- ✅ Validation des entrées
- ✅ Protection CSRF
- ✅ Security headers (Nginx)

### Configuration CORS

Modifier dans `CorsConfig.java` :
```java
configuration.setAllowedOrigins(List.of(
    "https://votre-frontend.com"
));
```

## 📊 Monitoring

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Métriques

Les métriques Actuator sont disponibles sur :
```
http://localhost:8080/actuator/metrics
```

### Logs

Les logs sont dans :
- Container : `/app/logs`
- Volume Docker : `app_logs`
```bash
# Voir les logs en temps réel
docker-compose logs -f app

# Ou avec Makefile
make logs-app
```

## 🐛 Dépannage

### Problème : Port 8080 déjà utilisé
```bash
# Trouver le processus
lsof -i :8080

# Tuer le processus
kill -9 PID
```

### Problème : Base de données inaccessible
```bash
# Vérifier que PostgreSQL est démarré
docker-compose ps postgres

# Vérifier les logs
docker-compose logs postgres

# Redémarrer
docker-compose restart postgres
```

### Problème : Erreur de connexion Redis
```bash
# Vérifier Redis
docker-compose ps redis

# Tester la connexion
docker-compose exec redis redis-cli ping
```

### Problème : Migration Flyway échoue
```bash
# Voir le statut des migrations
mvn flyway:info

# Réparer
mvn flyway:repair

# Nettoyer et recommencer (⚠️ ATTENTION : supprime toutes les données)
mvn flyway:clean flyway:migrate
```

## 🤝 Contribution

### Workflow

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

### Standards de Code

- Suivre les conventions Java
- Utiliser Lombok pour réduire le boilerplate
- Commenter le code complexe
- Écrire des tests pour les nouvelles fonctionnalités
- Respecter les principes SOLID

### Convention de Commit
```
feat: nouvelle fonctionnalité
fix: correction de bug
docs: documentation
style: formatage
refactor: refactoring
test: ajout de tests
chore: tâches de maintenance
```

## 📞 Support

Pour toute question ou problème :

- 📧 Email : support@djbc.com
- 📱 Téléphone : +226 XX XX XX XX
- 🌐 Site Web : https://djbc.com

## 📄 License

Ce projet est la propriété de **DJBC (Duty Free Burkina Company)**.  
Tous droits réservés © 2024 DJBC.

Usage strictement réservé à l'Aéroport International de Ouagadougou.

---

## 📝 Notes de Version

### Version 1.0.0 (Date de sortie)

#### Fonctionnalités
- ✅ Système de gestion des ventes complet
- ✅ Gestion multi-caisses
- ✅ Gestion des stocks avec sommiers
- ✅ Programme de fidélité
- ✅ Système de promotions
- ✅ Paiements multi-devises
- ✅ Reporting et analytics
- ✅ WebSocket pour temps réel

#### Améliorations Futures
- 🔄 Intégration avec TPE
- 🔄 Module de comptabilité avancé
- 🔄 Application mobile caissier
- 🔄 Dashboard analytics avancé
- 🔄 Intégration SMS pour notifications

---

**Développé avec ❤️ pour DJBC - Duty Free Burkina Company**