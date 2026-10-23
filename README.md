# AffiliateLink — module Profile

API Spring Boot pour créer et consulter les publishers et advertisers, puis gérer leur statut.

## Prérequis

- Java 17.
- Docker Desktop démarré avec Docker Compose.
- Maven Wrapper fourni dans le dépôt.

## Lancement local (Windows)

La configuration actuelle désactive le démarrage automatique de Docker Compose par Spring Boot. Démarrer PostgreSQL avant l’application :

```powershell
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Dans IntelliJ, lancer `com.affiliatelink.DemoApplication` après le démarrage de PostgreSQL.

- API : `http://localhost:8080`.
- PostgreSQL : port local `5433`, base `affiliatelink_db`.
- Les paramètres de connexion sont dans `src/main/resources/application.yaml`.
- Flyway applique les migrations ; Hibernate valide le schéma avec `ddl-auto: validate`.
- Après une modification du code ou un changement de branche, redémarrer l’application.

## Endpoints

| Méthode | Chemin | Réponses |
| --- | --- | --- |
| POST | /api/publishers | 201, 400, 409 |
| GET | /api/publishers | 200 |
| GET | /api/publishers/{id} | 200, 404 |
| POST | /api/advertisers | 201, 400, 409 |
| GET | /api/advertisers | 200 |
| GET | /api/advertisers/{id} | 200, 404 |
| PATCH | /api/admin/profiles/{id}/status | 204, 400, 404, 409 |

Les GET ne nécessitent aucun corps. Une liste sans résultat renvoie un tableau vide. Le PATCH réussi renvoie 204 sans corps.

## Tests avec Apidog

Créer un environnement local avec `baseUrl = http://localhost:8080` sans slash final. Pour POST et PATCH : Body JSON et `Content-Type: application/json`.

### Publisher

POST `{{baseUrl}}/api/publishers` :

```json
{
  "email": "publisher@example.com",
  "password": "TestJunior123!",
  "firstName": "Sara",
  "lastName": "Amrani",
  "niche": "TECH",
  "bio": "Tests de produits",
  "country": "Maroc"
}
```

Niches : BEAUTY, TECH, GAMING, TRAVEL, LIFESTYLE, FOOD, SPORT.

### Advertiser

POST `{{baseUrl}}/api/advertisers` :

```json
{
  "email": "advertiser@example.com",
  "password": "TestJunior123!",
  "companyName": "Demo SARL",
  "sector": "TECH",
  "website": "example.com"
}
```

Le mot de passe est un champ déjà présent dans cette implémentation, en supplément des champs du brief ; il comporte au moins huit caractères et est encodé avec BCrypt. Il ne figure jamais dans les DTO de sortie. Le site web est obligatoire, sans règle de format supplémentaire.

### Scénarios de création et consultation

1. Utiliser un email neuf : 201. Un nouveau compte est PENDING.
2. Réenvoyer le même email : 409. L’unicité est commune aux publishers et advertisers.
3. Envoyer un email invalide ou un champ obligatoire vide : 400.
4. Lire les deux listes : 200 et tableaux JSON, sans password.
5. Consulter un identifiant existant sur la route de son type : 200.
6. Consulter un identifiant absent ou appartenant à l’autre type : 404.

La création advertiser retourne son id et son status. Le DTO publisher actuel retourne les informations personnelles ; son identifiant est consultable dans la base.

### Statuts

Sur un advertiser neuf, relever l’id, puis envoyer PATCH `{{baseUrl}}/api/admin/profiles/{id}/status` :

```json
{"status":"ACTIVE"}
```

| Transition | Attendu |
| --- | --- |
| PENDING vers ACTIVE | 204 |
| ACTIVE vers SUSPENDED | 204 |
| SUSPENDED vers ACTIVE | 204 |
| Toute autre transition, y compris ACTIVE vers ACTIVE | 409 |
| Identifiant absent | 404 |
| Statut absent ou inconnu | 400 |

Après un changement, consulter l’advertiser par id pour vérifier le statut enregistré. Un compte n’est pas supprimé, il est suspendu.

## Organisation

Le domaine est regroupé dans `com.affiliatelink.profile` : entity, dto, repository, service, controller. Les exceptions et leur gestion sont dans `com.affiliatelink.common`.

Le contrôleur reçoit la requête, le service orchestre le traitement, le repository accède à PostgreSQL. Les transitions sont définies dans l’entité Profile. L’héritage JPA JOINED relie profile_profiles à profile_publishers ou profile_advertisers. Les entrées et sorties HTTP utilisent des records DTO.

Spring Modulith est prévu au brief suivant.

## Tests et livrable

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

ProfileStatusTest vérifie les neuf combinaisons de statuts. La commande package exécute les tests et produit `target/demo-0.0.1-SNAPSHOT.war`.

## Point restant du brief

Le démarrage automatique de PostgreSQL par Spring Boot reste à activer et vérifier : `spring.docker.compose.enabled` est actuellement `false`. Les fonctionnalités HTTP ont été vérifiées séparément ; cela ne valide pas cette exigence de démarrage automatique.

### Format des erreurs de saisie

Les erreurs de validation sont centralisées dans GlobalExceptionHandler et renvoient 400 avec les champs concernés :

```json
{"email":"Format d'email invalide"}
```

Un JSON incorrect, un corps manquant ou une valeur enum inconnue renvoie 400 avec un message JSON. Les valeurs saisies, notamment le mot de passe, ne sont pas reprises dans la réponse. Les erreurs métier restent en 404 (profil absent) ou 409 (email dupliqué ou transition interdite).
