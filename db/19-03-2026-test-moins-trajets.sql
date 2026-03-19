-- ==================================================
-- Base: operateur
-- Test cible: priorite au vehicule avec moins de trajets deja faits
-- Date: 19-03-2026
-- ==================================================

DROP DATABASE IF EXISTS operateur;
CREATE DATABASE operateur;
\c operateur;

-- -----------------------------
-- Schema
-- -----------------------------
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

-- -----------------------------
-- Parametres
-- -----------------------------
-- Fenetre de regroupement: 10 min
-- Un seul passager par groupe ici (horaires espaces d'1h)
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES (60.00, 30);

-- -----------------------------
-- Lieux
-- -----------------------------
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aeroport Ivato', 'AEROPORT'),
('H01', 'Hotel Ibis', 'HOTEL');

-- Distances
-- 10 km aller, 10 km retour
INSERT INTO distance (from_id, to_id, km) VALUES
(1, 2, 10.0),
(2, 1, 10.0);

-- -----------------------------
-- Vehicules
-- -----------------------------
-- V1 et V2 ont meme capacite (5)
-- Au debut: meme nb de trajets (0)
-- Donc diesel (V1) passe avant essence (V2)
INSERT INTO vehicule (immatriculation, capacite, carburant) VALUES
('V1-DIESEL-5', 5, 'D'),
('V2-ESSENCE-5', 5, 'E'),
('V3-DIESEL-8', 8, 'D');

-- -----------------------------
-- Reservations
-- -----------------------------
-- Chaque reservation cree un groupe different (08h, 09h, 10h, 11h)
-- Toutes font 4 passagers => V1 et V2 sont tous les deux capables
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-1', 4, '2026-03-19 08:00:00', 2),
('Client-2', 4, '2026-03-19 09:00:00', 2),
('Client-3', 4, '2026-03-19 10:00:00', 2),
('Client-4', 4, '2026-03-19 11:00:00', 2);

-- ==================================================
-- RESULTAT ATTENDU APRES EXECUTION DE affecterVehicules('2026-03-19')
-- ==================================================
-- Groupe 08:00 -> V1 (diesel prioritaire, trajets egaux)
-- Groupe 09:00 -> V2 (moins de trajets que V1)
-- Groupe 10:00 -> V1 (trajets egaux a nouveau, diesel prioritaire)
-- Groupe 11:00 -> V2 (moins de trajets que V1)

-- Requete de verification (a lancer apres affectation):
-- SELECT
--   r.id,
--   r.id_client,
--   r.date_heure_depart AS reservation_depart,
--   a.date_heure_depart AS depart_effectif,
--   v.immatriculation,
--   v.capacite,
--   v.carburant
-- FROM affectation a
-- JOIN reservation r ON r.id = a.id_reservation
-- JOIN vehicule v ON v.id = a.id_vehicule
-- ORDER BY r.date_heure_depart;
