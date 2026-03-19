-- ==================================================
-- Base: operateur
-- Test complet: 5 vehicules + cas multiples
-- Date de test: 2026-03-20
-- Regles ciblees:
-- 1) Groupement par fenetre temps_attente
-- 2) Depart = max(derniere reservation du groupe, retour vehicule)
-- 3) Priorite: capacite, moins de trajets, diesel, aleatoire si egalite parfaite
-- 4) Decoupage du groupe si aucun vehicule assez grand
-- 5) Reservation non affectee si aucune capacite possible
-- 6) Tie-break de route (distance egale -> ordre alphabetique du lieu)
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
    nombre_passagers_affectes INT NOT NULL,
    FOREIGN KEY (id_vehicule) REFERENCES vehicule(id),
    FOREIGN KEY (id_reservation) REFERENCES reservation(id)
);

-- -----------------------------
-- Parametres
-- -----------------------------
-- temps_attente utilise seulement pour former les groupes
-- vitesse 60 km/h
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES (60.00, 10);

-- -----------------------------
-- Lieux
-- -----------------------------
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aeroport Ivato', 'AEROPORT'),
('H01', 'Hotel Ibis', 'HOTEL'),
('H02', 'Hotel Alpha', 'HOTEL'),
('H03', 'Hotel Beta', 'HOTEL'),
('H04', 'Hotel Carlton', 'HOTEL');

-- -----------------------------
-- Distances (matrice dirigee complete)
-- -----------------------------
INSERT INTO distance (from_id, to_id, km) VALUES
-- Aeroport -> Hotels
(1,2,10.0), (1,3,12.0), (1,4,12.0), (1,5,18.0),
-- Hotels -> Aeroport
(2,1,10.0), (3,1,12.0), (4,1,12.0), (5,1,18.0),
-- Hotel Ibis <-> autres
(2,3,6.0),  (3,2,6.0),
(2,4,8.0),  (4,2,8.0),
(2,5,12.0), (5,2,12.0),
-- Hotel Alpha <-> autres
(3,4,7.0),  (4,3,7.0),
(3,5,9.0),  (5,3,9.0),
-- Hotel Beta <-> Carlton
(4,5,5.0),  (5,4,5.0);

-- -----------------------------
-- Vehicules (5 vehicules)
-- -----------------------------
INSERT INTO vehicule (immatriculation, capacite, carburant) VALUES
('V1-DIESEL-4',   4,  'D'),
('V2-ESSENCE-4',  4,  'E'),
('V3-DIESEL-6',   6,  'D'),
('V4-ESSENCE-8',  8,  'E'),
('V5-DIESEL-12', 12,  'D');

-- -----------------------------
-- Reservations (cas multiples)
-- -----------------------------
-- G1 [08:00-08:10] => R1,R2,R3 (total 9) -> un seul vehicule (V5)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-01', 5, '2026-03-20 08:00:00', 2),
('Client-02', 3, '2026-03-20 08:05:00', 3),
('Client-03', 1, '2026-03-20 08:09:00', 4);

-- G2 [08:30-08:40] => R4,R5 (total 8) -> V4 exact
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-04', 4, '2026-03-20 08:30:00', 3),
('Client-05', 4, '2026-03-20 08:38:00', 3);

-- G3 [08:45-08:55] => R6 (4) -> V1 (diesel prioritaire quand trajets egaux)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-06', 4, '2026-03-20 08:45:00', 2);

-- G4 [09:00-09:10] => R7 (4) -> V2 (moins de trajets que V1)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-07', 4, '2026-03-20 09:00:00', 2);

-- G5 [09:20-09:30] => R8,R9 (total 13) -> split car aucun vehicule >=13
-- R8(7) -> V4 ; R9(6) -> V3
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-08', 7, '2026-03-20 09:20:00', 5),
('Client-09', 6, '2026-03-20 09:25:00', 4);

-- G6 [09:35-09:45] => R10 (8)
-- V4 est prioritaire (capacite 8), mais occupe jusqu'a 10:01 -> depart decale a 10:01
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-10', 8, '2026-03-20 09:35:00', 3);

-- G7 [11:00-11:10] => R11 (15) -> non affectee (aucun vehicule capable)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-11', 15, '2026-03-20 11:00:00', 2);

-- G8 [12:00-12:10] => R12,R13 (destinations Alpha & Beta)
-- Distance egale depuis aeroport (12 et 12) -> ordre alphabetique: Alpha avant Beta
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-12', 2, '2026-03-20 12:00:00', 3),
('Client-13', 2, '2026-03-20 12:05:00', 4);

-- ==================================================
-- RESULTATS ATTENDUS (apres affecterVehicules('2026-03-20'))
-- ==================================================
-- G1: V5-DIESEL-12 depart 08:09 retour 08:44 (35 min)
-- G2: V4-ESSENCE-8 depart 08:38 retour 09:02 (24 min)
-- G3: V1-DIESEL-4 depart 08:45 retour 09:05 (20 min)
-- G4: V2-ESSENCE-4 depart 09:00 retour 09:20 (20 min)
-- G5 split:
--   - R8(7) sur V4 depart 09:25 retour 10:01 (36 min)
--   - R9(6) sur V3 depart 09:25 retour 09:49 (24 min)
-- G6: V4 prioritaire mais occupe, donc depart 10:01 retour 10:25 (24 min)
-- G7: R11 non affectee (15 > capacite max 12)
-- G8: V1 (ou V2 selon trajets/capacite dispo) avec ordre de livraison Alpha puis Beta

-- ==================================================
-- Requetes de verification
-- ==================================================

-- 1) Affectations detaillees
-- SELECT
--   r.id,
--   r.id_client,
--   r.nombre_passagers,
--   to_char(r.date_heure_depart, 'HH24:MI') AS reservation_heure,
--   v.immatriculation,
--   to_char(a.date_heure_depart, 'HH24:MI') AS depart_effectif,
--   to_char(a.date_heure_retour, 'HH24:MI') AS retour_effectif,
--   a.ordre_livraison,
--   l.libelle AS destination
-- FROM affectation a
-- JOIN reservation r ON r.id = a.id_reservation
-- JOIN vehicule v ON v.id = a.id_vehicule
-- JOIN lieu l ON l.id = r.id_lieu_destination
-- ORDER BY a.date_heure_depart, v.immatriculation, a.ordre_livraison, r.id;

-- 2) Resume par voyage
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
-- ORDER BY a.date_heure_depart, v.immatriculation;

-- 3) Reservations non affectees
-- SELECT r.*
-- FROM reservation r
-- LEFT JOIN affectation a ON a.id_reservation = r.id
-- WHERE a.id IS NULL
-- ORDER BY r.date_heure_depart;
