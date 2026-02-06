# Application de Réservation d'Hôtel

Application web développée avec un mini-framework Spring MVC custom.

## Prérequis

1. **Java JDK 17+**
2. **Apache Tomcat 9.0.109** (installé dans `C:\Bossy\apache-tomcat-9.0.109`)
3. **PostgreSQL** (base de données)
4. **Bibliothèques JAR** à placer dans le dossier `lib/` :
   - `myframework.jar` (déjà présent)
   - `postgresql-42.7.1.jar` ([télécharger](https://jdbc.postgresql.org/download/))
   - `gson-2.10.1.jar` ([télécharger](https://search.maven.org/artifact/com.google.code.gson/gson/2.10.1/jar))
   - `jakarta.servlet-api-5.0.0.jar` ([télécharger](https://search.maven.org/artifact/jakarta.servlet/jakarta.servlet-api/5.0.0/jar))

## Configuration de la Base de Données

### 1. Créer la base de données PostgreSQL

```sql
CREATE DATABASE hotel_db;
```

### 2. Exécuter le script de création des tables

```bash
psql -U postgres -d hotel_db -f base.sql
```

### 3. Insérer les hôtels de test

```bash
psql -U postgres -d hotel_db -f db/seed_hotels.sql
```

### 4. Configurer la connexion

Modifier les paramètres dans `src/config/DbConfig.java` si nécessaire :
```java
public static final String URL = "jdbc:postgresql://localhost:5432/hotel_db";
public static final String USER = "postgres";
public static final String PASSWORD = "postgres";
```

## Structure du Projet

```
Asignation/
├── base.sql                        # Script création tables
├── db/
│   └── seed_hotels.sql             # Données de test hôtels
├── lib/
│   ├── myframework.jar             # Framework custom
│   ├── postgresql-42.x.jar         # Driver JDBC PostgreSQL
│   ├── gson-2.x.jar                # Bibliothèque JSON
│   └── jakarta.servlet-api-5.0.0.jar
├── src/
│   ├── config/
│   │   └── DbConfig.java           # Configuration DB
│   ├── dao/
│   │   ├── HotelDao.java           # Accès données hôtels
│   │   └── ReservationDao.java     # Accès données réservations
│   ├── model/
│   │   ├── Hotel.java              # Entité Hôtel
│   │   └── Reservation.java        # Entité Réservation
│   └── controller/
│       └── ReservationController.java  # Contrôleur principal
├── webapp/
│   ├── WEB-INF/
│   │   └── web.xml                 # Descripteur déploiement
│   └── views/
│       ├── reservation-form.jsp    # Formulaire de réservation
│       └── reservation-success.jsp # Page de confirmation
├── build.bat                       # Script de build & déploiement
└── README.md
```

## Compilation et Déploiement

### Option 1 : Script automatique (Windows)

```batch
build.bat
```

### Option 2 : Manuel

1. **Compiler les sources**
```batch
mkdir build\WEB-INF\classes
mkdir build\WEB-INF\lib
javac -d build\WEB-INF\classes -cp "lib\*" src\config\*.java src\model\*.java src\dao\*.java src\controller\*.java
```

2. **Copier les fichiers**
```batch
xcopy /E /Y webapp\* build\
copy lib\*.jar build\WEB-INF\lib\
```

3. **Créer le WAR**
```batch
cd build
jar -cvf reservation.war .
```

4. **Déployer**
```batch
copy build\reservation.war C:\Bossy\apache-tomcat-9.0.109\webapps\
```

## Démarrer Tomcat

```batch
C:\Bossy\apache-tomcat-9.0.109\bin\startup.bat
```

## Utilisation

### Formulaire de Réservation
**URL** : http://localhost:8080/reservation/reservations/new

Le formulaire permet de :
- Saisir un **identifiant client** (exactement 4 caractères alphanumériques, ex: `AB12`, `X7Y9`)
- Saisir le **nombre de passagers**
- Sélectionner une **date et heure**
- Choisir un **hôtel** parmi ceux de la base de données

### API REST - Liste des Réservations
**URL** : http://localhost:8080/reservation/api/reservations  
**Méthode** : GET  
**Format** : JSON

**Exemple de réponse** :
```json
{
  "status": "success",
  "code": 200,
  "data": [
    {
      "id": 1,
      "idClient": "AB12",
      "nbpassagers": 3,
      "dateheure": "2026-02-15T14:30:00",
      "idHotel": 1,
      "hotelNom": "Hôtel du Centre"
    }
  ],
  "count": 1
}
```

## Annotations du Framework

| Annotation | Description |
|------------|-------------|
| `@MyController` | Marque une classe comme contrôleur |
| `@MyMapping("url")` | Définit l'URL de la route |
| `@GET` | Méthode HTTP GET |
| `@POST` | Méthode HTTP POST |
| `@Json` | Retourne la réponse en JSON |
| `@RequestParam` | Lie un paramètre de requête |

## Arrêter Tomcat

```batch
C:\Bossy\apache-tomcat-9.0.109\bin\shutdown.bat
```
