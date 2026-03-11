-- =============================================
-- Base de données: operateur
-- Date: 11-03-2026
-- Objectif: Illustrer le temps d'attente (regroupement)
-- =============================================

DROP DATABASE IF EXISTS operateur;
CREATE DATABASE operateur;
\c operateur;

-- Tables
CREATE TABLE parametre (
    id SERIAL PRIMARY KEY,
    vitesse_moyenne DECIMAL(10, 2) NOT NULL,
    temps_attente INT NOT NULL
);

CREATE TABLE lieu (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('HOTEL', 'AEROPORT'))
);

CREATE TABLE distance (
    id SERIAL PRIMARY KEY,
    from_id INT NOT NULL,
    to_id INT NOT NULL,
    km DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (from_id) REFERENCES lieu(id),
    FOREIGN KEY (to_id) REFERENCES lieu(id),
    UNIQUE (from_id, to_id)
);

CREATE TABLE vehicule (
    id SERIAL PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    capacite INT NOT NULL,
    carburant CHAR(1) NOT NULL CHECK (carburant IN ('D', 'E'))
);

CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(255) NOT NULL,
    nombre_passagers INT NOT NULL,
    date_heure_depart TIMESTAMP NOT NULL,
    id_lieu_destination INT NOT NULL,
    FOREIGN KEY (id_lieu_destination) REFERENCES lieu(id)
);

CREATE TABLE affectation (
    id SERIAL PRIMARY KEY,
    id_vehicule INT NOT NULL,
    id_reservation INT NOT NULL,
    date_heure_depart TIMESTAMP NOT NULL,
    date_heure_retour TIMESTAMP NOT NULL,
    ordre_livraison INT NOT NULL DEFAULT 1,
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id),
    FOREIGN KEY (id_reservation) REFERENCES reservation(id),
    UNIQUE (id_reservation)
);

-- =============================================
-- DONNÉES
-- =============================================

-- temps_attente = 30 minutes (fenêtre de regroupement)
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES (60.00, 30);

-- Lieux
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aéroport Ivato', 'AEROPORT'),
('H01', 'Hôtel Ibis', 'HOTEL'),
('H02', 'Hôtel Carlton', 'HOTEL');

-- Distances
INSERT INTO distance (from_id, to_id, km) VALUES
(1, 2, 10.0), (2, 1, 10.0),  -- Aéroport <-> Ibis
(1, 3, 15.0), (3, 1, 15.0),  -- Aéroport <-> Carlton
(2, 3, 8.0),  (3, 2, 8.0);   -- Ibis <-> Carlton

-- Véhicules
INSERT INTO vehicule (immatriculation, capacite, carburant) VALUES
('1234 ABC', 10, 'D'),
('5678 DEF', 5, 'D'),
('9012 GHI', 4, 'E');

-- =============================================
-- RÉSERVATIONS - Illustration du temps d'attente
-- =============================================
-- 3 clients arrivent à 08:00, 08:10, 08:20
-- Avec temps_attente = 30 min, ils sont regroupés
-- Départ effectif = 08:00 + 30 min = 08:30

INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client A', 6, '2026-03-11 08:00:00', 2),  -- Arrive à 08:00 → Ibis (6 passagers)
('Client B', 3, '2026-03-11 08:10:00', 3),  -- Arrive à 08:10 → Carlton (3 passagers)
('Client C', 1, '2026-03-11 08:20:00', 2),  -- Arrive à 08:20 → Ibis (1 passager)
('Client D', 4, '2026-03-11 10:00:00', 3);  -- Arrive à 10:00 → Carlton (4 passagers)

-- RÉSULTAT ATTENDU:
-- Groupe 08:00-08:30: 10 passagers → Véhicule 1234 ABC (10 places), départ 08:30
-- Groupe 10:00-10:30: 4 passagers → Véhicule 9012 GHI (4 places) ou 5678 DEF (5 places), départ 10:30
