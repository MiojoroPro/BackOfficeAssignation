-- ==================================================
-- Base: operateur
-- Test cible: heure depart = max(derniere reservation du groupe, retour vehicule)
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
-- temps_attente = 30 min => uniquement pour le regroupement
-- vitesse = 60 km/h
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES (50.00, 30);

-- -----------------------------
-- Lieux
-- -----------------------------
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aeroport Ivato', 'AEROPORT'),
('H01', 'Hotel Ibis', 'HOTEL');

-- Distances
-- AER -> H01 = 10 km, H01 -> AER = 10 km
INSERT INTO distance (from_id, to_id, km) VALUES
(1, 2, 10.0),
(2, 1, 10.0);

-- -----------------------------
-- Vehicules
-- -----------------------------
-- Un seul vehicule capable (5 places)
-- Le second est volontairement insuffisant (3 places)
INSERT INTO vehicule (immatriculation, capacite, carburant) VALUES
('V1-DIESEL-5', 5, 'D'),
('V2-ESSENCE-3', 3, 'E');

-- -----------------------------
-- Reservations
-- -----------------------------
-- Groupe 1 (fenetre 08:00 -> 08:30): R1 + R2
-- derniere reservation du groupe = 08:20
--
-- Groupe 2 (fenetre 08:40 -> 09:10): R3
-- derniere reservation du groupe = 08:40
-- mais vehicule revient de G1 a 08:44 => depart attendu 08:44
--
-- Groupe 3 (fenetre 10:05 -> 10:35): R4
-- derniere reservation du groupe = 10:05
-- vehicule libre a 10:00 => depart attendu 10:05
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-1', 2, '2026-03-19 08:00:00', 2),
('Client-2', 2, '2026-03-19 08:20:00', 2),
('Client-3', 4, '2026-03-19 08:40:00', 2),
('Client-4', 4, '2026-03-19 10:05:00', 2);

-- ==================================================
-- RESULTATS ATTENDUS APRES affecterVehicules('2026-03-19')
-- ==================================================
-- Temps trajet pour un voyage avec destination unique:
-- vitesse = 50 km/h, sans temps d'arret de livraison
-- AER->HOTEL = ceil(10/50*60)=12 min, HOTEL->AER=12 min => total 24 min
--
-- Voyage 1 (R1+R2): depart 08:20, retour 08:44
-- Voyage 2 (R3):    depart 08:44, retour 09:08
-- Voyage 3 (R4):    depart 10:05, retour 10:29
--
-- Toutes les reservations sont affectees sur V1-DIESEL-5

-- Verification detaillee
-- SELECT
--   r.id,
--   r.id_client,
--   to_char(r.date_heure_depart, 'HH24:MI') AS reservation_heure,
--   v.immatriculation,
--   to_char(a.date_heure_depart, 'HH24:MI') AS depart_effectif,
--   to_char(a.date_heure_retour, 'HH24:MI') AS retour_effectif,
--   a.ordre_livraison
-- FROM affectation a
-- JOIN reservation r ON r.id = a.id_reservation
-- JOIN vehicule v ON v.id = a.id_vehicule
-- ORDER BY a.date_heure_depart, r.id;

-- Verification par voyage
-- SELECT
--   v.immatriculation,
--   to_char(a.date_heure_depart, 'HH24:MI') AS depart,
--   to_char(a.date_heure_retour, 'HH24:MI') AS retour,
--   count(*) AS nb_reservations,
--   sum(r.nombre_passagers) AS total_passagers
-- FROM affectation a
-- JOIN reservation r ON r.id = a.id_reservation
-- JOIN vehicule v ON v.id = a.id_vehicule
-- GROUP BY v.immatriculation, a.date_heure_depart, a.date_heure_retour
-- ORDER BY a.date_heure_depart;
