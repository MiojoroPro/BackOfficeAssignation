-- ==================================================
-- SCHEMA ACTUEL - VERSION DECOPUAGE RESERVATION
-- Base cible: operateur
--
-- Ce fichier contient UNIQUEMENT le format des tables.
-- Les evolutions importantes sont taguees [AJOUTE] / [MODIFIE].
-- ==================================================

DROP DATABASE IF EXISTS operateur;
CREATE DATABASE operateur;
\c operateur;

-- =========================
-- TABLE parametre
-- =========================
CREATE TABLE parametre (
    id SERIAL PRIMARY KEY,
    vitesse_moyenne DECIMAL(10, 2) NOT NULL,
    temps_attente INT NOT NULL
);

-- =========================
-- TABLE lieu
-- =========================
CREATE TABLE lieu (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('HOTEL', 'AEROPORT'))
);

-- =========================
-- TABLE distance
-- =========================
CREATE TABLE distance (
    id SERIAL PRIMARY KEY,
    from_id INT NOT NULL,
    to_id INT NOT NULL,
    km DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (from_id) REFERENCES lieu(id),
    FOREIGN KEY (to_id) REFERENCES lieu(id),
    UNIQUE (from_id, to_id)
);

-- =========================
-- TABLE vehicule
-- =========================
CREATE TABLE vehicule (
    id SERIAL PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    capacite INT NOT NULL,
    carburant CHAR(1) NOT NULL CHECK (carburant IN ('D', 'E')),

    -- [AJOUTE] disponibilité journalière du véhicule (00:00 par défaut)
    heure_disponibilite TIME NOT NULL DEFAULT '00:00:00'
);

-- =========================
-- TABLE reservation
-- =========================
CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(255) NOT NULL,
    nombre_passagers INT NOT NULL,
    date_heure_depart TIMESTAMP NOT NULL,
    id_lieu_destination INT NOT NULL,
    FOREIGN KEY (id_lieu_destination) REFERENCES lieu(id)
);

-- =========================
-- TABLE affectation
-- =========================
CREATE TABLE affectation (
    id SERIAL PRIMARY KEY,
    id_vehicule INT NOT NULL,
    id_reservation INT NOT NULL,
    date_heure_depart TIMESTAMP NOT NULL,
    date_heure_retour TIMESTAMP NOT NULL,
    ordre_livraison INT NOT NULL DEFAULT 1,

    -- [AJOUTE] nombre reel de passagers pris par cette ligne d'affectation
    -- (utile quand une reservation est decoupee sur plusieurs vehicules)
    nombre_passagers_affectes INT NOT NULL,

    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id),
    FOREIGN KEY (id_reservation) REFERENCES reservation(id)

    -- [MODIFIE] la contrainte UNIQUE(id_reservation) a ete retiree
    -- pour autoriser plusieurs affectations pour la meme reservation
);
