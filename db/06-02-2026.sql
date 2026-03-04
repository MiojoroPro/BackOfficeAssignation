DROP DATABASE IF EXISTS assignation;
CREATE DATABASE assignation;
\c assignation;

-- =============================================
-- TABLE PARAMETRE
-- Stocke les valeurs globales pour les calculs
-- =============================================
CREATE TABLE parametre (
    id SERIAL PRIMARY KEY,
    vitesse_moyenne DECIMAL(10, 2) NOT NULL, -- km/h
    temps_attente INT NOT NULL -- minutes d'attente par réservation
);

-- =============================================
-- TABLE LIEU
-- Stocke les hôtels et l'aéroport
-- =============================================
CREATE TABLE lieu (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('HOTEL', 'AEROPORT'))
);

-- =============================================
-- TABLE DISTANCE
-- Stocke les distances entre deux lieux
-- =============================================
CREATE TABLE distance (
    id SERIAL PRIMARY KEY,
    from_id INT NOT NULL,
    to_id INT NOT NULL,
    km DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (from_id) REFERENCES lieu(id),
    FOREIGN KEY (to_id) REFERENCES lieu(id),
    UNIQUE (from_id, to_id)
);

-- =============================================
-- TABLE VEHICULE
-- Stocke les véhicules disponibles
-- =============================================
CREATE TABLE vehicule (
    id SERIAL PRIMARY KEY,
    immatriculation VARCHAR(20) NOT NULL UNIQUE,
    capacite INT NOT NULL,
    carburant CHAR(1) NOT NULL CHECK (carburant IN ('D', 'E')) -- D=Diesel, E=Essence
);

-- =============================================
-- TABLE RESERVATION
-- Stocke les réservations de clients
-- =============================================
CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    id_client VARCHAR(255) NOT NULL,
    nombre_passagers INT NOT NULL,
    date_heure_depart TIMESTAMP NOT NULL, -- Date et heure de départ de l'aéroport
    id_lieu_destination INT NOT NULL, -- Lieu de destination (hôtel)
    FOREIGN KEY (id_lieu_destination) REFERENCES lieu(id)
);

-- =============================================
-- TABLE AFFECTATION
-- Relie les véhicules aux réservations
-- =============================================
CREATE TABLE affectation (
    id SERIAL PRIMARY KEY,
    id_vehicule INT NOT NULL,
    id_reservation INT NOT NULL,
    date_heure_depart TIMESTAMP NOT NULL, -- Heure de départ effective
    date_heure_retour TIMESTAMP NOT NULL, -- Heure de retour calculée
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id),
    FOREIGN KEY (id_reservation) REFERENCES reservation(id),
    UNIQUE (id_reservation) -- Une réservation ne peut avoir qu'une affectation
);

-- =============================================
-- DONNÉES DE TEST
-- =============================================

-- Paramètres globaux
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES (60.00, 15);

-- Lieux (Aéroport + Hôtels)
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aéroport Ivato', 'AEROPORT'),
('H01', 'Hôtel Ibis', 'HOTEL'),
('H02', 'Hôtel Carlton', 'HOTEL'),
('H03', 'Hôtel Colbert', 'HOTEL'),
('H04', 'Hôtel du Louvre', 'HOTEL'),
('H05', 'Hôtel Panorama', 'HOTEL');

-- Distances (de l'aéroport vers les hôtels et vice versa)
INSERT INTO distance (from_id, to_id, km) VALUES
-- Aéroport vers hôtels
(1, 2, 15.5),  -- AER -> H01
(1, 3, 18.0),  -- AER -> H02
(1, 4, 12.0),  -- AER -> H03
(1, 5, 25.0),  -- AER -> H04
(1, 6, 30.0),  -- AER -> H05
-- Hôtels vers aéroport
(2, 1, 15.5),  -- H01 -> AER
(3, 1, 18.0),  -- H02 -> AER
(4, 1, 12.0),  -- H03 -> AER
(5, 1, 25.0),  -- H04 -> AER
(6, 1, 30.0),  -- H05 -> AER
-- Entre hôtels
(2, 3, 5.0),
(2, 4, 8.0),
(2, 5, 12.0),
(2, 6, 18.0),
(3, 2, 5.0),
(3, 4, 10.0),
(3, 5, 15.0),
(3, 6, 20.0),
(4, 2, 8.0),
(4, 3, 10.0),
(4, 5, 14.0),
(4, 6, 22.0),
(5, 2, 12.0),
(5, 3, 15.0),
(5, 4, 14.0),
(5, 6, 10.0),
(6, 2, 18.0),
(6, 3, 20.0),
(6, 4, 22.0),
(6, 5, 10.0);

-- Véhicules (4 véhicules pour illustrer les règles de gestion)
-- Règle: capacité >= passagers, capacité la plus proche, priorité Diesel, sinon random
INSERT INTO vehicule (immatriculation, capacite, carburant) VALUES
('1111 TAA', 11, 'D'),  -- 11 places Diesel
('2222 TAB', 11, 'D'),  -- 11 places Diesel
('3333 TAC', 11, 'E'),  -- 11 places Essence
('4444 TAD', 18, 'D');  -- 18 places Diesel

-- Réservation de test (date: 03-03-2026)
-- 8 passagers → véhicules dispo: 11(D), 11(D), 11(E), 18(D)
-- Attendu: choisir parmi les 11 places Diesel (random entre 1111 TAA et 2222 TAB)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('CLI001', 8, '2026-03-03 08:00:00', 2);  -- 8 passagers vers Hôtel Ibis

-- =============================================
-- VUE: Liste des réservations avec détails
-- =============================================
CREATE OR REPLACE VIEW v_reservation_details AS
SELECT 
    r.id,
    r.id_client,
    r.nombre_passagers,
    r.date_heure_depart,
    DATE(r.date_heure_depart) as date_reservation,
    l.code as code_lieu_destination,
    l.libelle as lieu_destination,
    aer.code as code_lieu_depart,
    aer.libelle as lieu_depart
FROM reservation r
JOIN lieu l ON r.id_lieu_destination = l.id
CROSS JOIN (SELECT * FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer;

-- =============================================
-- VUE: Affectations avec détails complets
-- =============================================
CREATE OR REPLACE VIEW v_affectation_details AS
SELECT 
    a.id as id_affectation,
    v.id as id_vehicule,
    v.immatriculation,
    v.capacite,
    v.carburant,
    r.id as id_reservation,
    r.id_client,
    r.nombre_passagers,
    aer.libelle as lieu_depart,
    l.libelle as lieu_arrivee,
    a.date_heure_depart,
    a.date_heure_retour
FROM affectation a
JOIN vehicule v ON a.id_vehicule = v.id
JOIN reservation r ON a.id_reservation = r.id
JOIN lieu l ON r.id_lieu_destination = l.id
CROSS JOIN (SELECT * FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer;

-- =============================================
-- VUE: Réservations non affectées
-- =============================================
CREATE OR REPLACE VIEW v_reservations_non_affectees AS
SELECT 
    r.id,
    r.id_client,
    r.nombre_passagers,
    r.date_heure_depart,
    DATE(r.date_heure_depart) as date_reservation,
    l.libelle as lieu_destination
FROM reservation r
JOIN lieu l ON r.id_lieu_destination = l.id
WHERE r.id NOT IN (SELECT id_reservation FROM affectation);
