# AffiliateLink — module Profile

API Spring Boot pour créer et consulter les publishers et advertisers, puis gérer leur statut.

## Prérequis

- Java 17.
- Docker Desktop démarré avec Docker Compose.
- Maven Wrapper fourni dans le dépôt.

## Lancement local (Windows)

Avec Docker Desktop ouvert, Spring Boot démarre PostgreSQL automatiquement via Docker Compose :

```powershell
.\mvnw.cmd spring-boot:run
```

Dans IntelliJ, lancer com.affiliatelink.DemoApplication avec le dossier du projet comme répertoire de travail.

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

ProfileStatusTest vérifie les neuf combinaisons de statuts. La commande package exécute les tests et produit `target/demo-0.0.1-SNAPSHOT.jar`.

## Docker Compose

Le démarrage automatique est activé avec spring.docker.compose.enabled: true. Spring Boot utilise docker-compose.yml ; Docker Desktop doit être ouvert. Si PostgreSQL tourne déjà, il est réutilisé. Les données restent dans le volume Docker. Le port API habituel reste 8080.

### Format des erreurs de saisie

Les erreurs de validation sont centralisées dans GlobalExceptionHandler et renvoient 400 avec les champs concernés :

```json
{"email":"Format d'email invalide"}
```

Un JSON incorrect, un corps manquant ou une valeur enum inconnue renvoie 400 avec un message JSON. Les valeurs saisies, notamment le mot de passe, ne sont pas reprises dans la réponse. Les erreurs métier restent en 404 (profil absent) ou 409 (email dupliqué ou transition interdite).

Pour lancer le JAR depuis le dossier du projet, avec Docker Desktop ouvert :

```powershell
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Architecture modulaire

Spring Modulith 2.1.1 vérifie les frontières des modules. Le module `profile` expose uniquement les types publics de son package racine :

- `ProfileApi` : `advertiserExists(id)` et `isAdvertiserActive(id)` ; un identifiant absent renvoie false, un profil publisher ne compte pas comme advertiser.
- `ProfileSuspended(profileId)` : événement publié lors d’une transition autorisée vers SUSPENDED, dans la transaction de changement de statut.

Les contrôleurs, DTO HTTP, services, repositories et entités se trouvent sous `profile.internal`. Les autres modules ne doivent pas les importer. `common` est un module ouvert pour les erreurs communes. L’API Java intermodules est différente des endpoints HTTP : aucun endpoint supplémentaire n’est ajouté.

Le futur module offer utilisera ProfileApi et écoutera ProfileSuspended après commit. Le registre persistant JPA et son listener seront ajoutés à ce checkpoint ultérieur ; la publication actuelle seule ne garantit pas une livraison persistante.

Vérification : `./mvnw.cmd clean test`. Le test `ModularityTest` exécute `ApplicationModules.of(DemoApplication.class).verify()`. Les tests HTTP sans base vérifient les sept routes et des réponses 400/404/409 ; ils ne remplacent pas une recette avec PostgreSQL dans Apidog.

## Module Offer — modèle de données

Le checkpoint de domaine ajoute `Offre`, `Campaign` et `Tag` sous `offer.internal.entity`, leurs repositories et la migration `V3__create_offer_schema.sql`.

- Une offre possède plusieurs campagnes ; Campaign porte la clé étrangère `offre_id`.
- Les tags sont partagés entre offres via `offer_offre_tags`, avec un nom normalisé et unique.
- `advertiserId` est un identifiant simple : aucune relation JPA ni clé étrangère vers les tables profile.
- Montants : BigDecimal / NUMERIC(19,4). Dates de campagne : LocalDate. Statuts et modèles de rémunération : enums enregistrés en texte.
- `niche` est un texte propre au module offer ; aucune dépendance vers l’enum interne du module profile.
- Les règles de transitions et les endpoints offer seront ajoutés aux checkpoints suivants.

Les tests ordinaires se lancent avec `.\mvnw.cmd test`. Pour vérifier aussi la persistance, démarrer PostgreSQL avec Docker Desktop ouvert et `docker compose up -d postgres`, puis lancer :

```powershell
.\mvnw.cmd test '-Dtest=OfferPersistenceIT'
```

Ce test charge le contexte, applique les migrations, relit les relations depuis PostgreSQL et vérifie l’unicité des tags. Ses données d’essai sont annulées en fin de transaction ; les migrations de schéma restent appliquées.

## Créer une offre — US1

POST `/api/offres`, Content-Type `application/json` :

```json
{
  "advertiserId": 1,
  "title": "Formation Java",
  "description": "Formation Java pour débutants",
  "landingUrl": "https://example.com/formation-java",
  "niche": "TECH",
  "tags": [" Java ", "java", "Formation"]
}
```

Remplacer advertiserId par un annonceur existant ACTIVE. Réponses : 201 avec DTO de l’offre, id et createdAt ; 400 pour saisie invalide ; 404 si l’annonceur est absent ou si l’id correspond à un publisher ; 409 si l’annonceur est inactif. Une collision entre deux créations concurrentes du même tag renvoie aussi 409 et invite à réessayer.

Les tags sont facultatifs ; absents, null ou [] donnent une collection vide. Les noms sont normalisés en minuscules et sans espaces en bordure ; un tag déjà présent est réutilisé. La réponse contient des objets tags `{ "id": ..., "name": "java" }`, pas les entités JPA. Le service est transactionnel et utilise ProfileApi. Les conversions DTO/entités sont générées par MapStruct. L’identifiant de l’annonceur est fourni dans le corps pour ce contrat pédagogique ; aucune authentification de l’appelant n’est ajoutée à ce checkpoint.

Pour tester automatiquement US1 avec PostgreSQL actif :

```powershell
.\mvnw.cmd test '-Dtest=OfferCreationIT'
```

Les tests ordinaires et ModularityTest restent exécutables sans PostgreSQL. Les GET /api/offres et PUT ne sont pas encore implémentés à US1.
