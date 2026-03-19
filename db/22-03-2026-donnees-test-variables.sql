-- ==================================================
-- DONNEES DE TEST VARIABLES
-- Base cible: operateur
-- Usage:
-- 1) Executer d'abord 22-03-2026.sql
-- 2) Adapter les valeurs ci-dessous selon le scenario voulu
-- 3) Lancer l'affectation pour la date de test
-- ==================================================

\c operateur;

-- Nettoyage des donnees metier (schema conserve)
TRUNCATE TABLE affectation RESTART IDENTITY CASCADE;
TRUNCATE TABLE reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE lieu RESTART IDENTITY CASCADE;
TRUNCATE TABLE parametre RESTART IDENTITY CASCADE;

-- -----------------------------
-- PARAMETRES (VARIABLES)
-- -----------------------------
-- Modifier ces valeurs selon vos tests:
-- vitesse_moyenne: impacte la duree de trajet
-- temps_attente: impacte uniquement le regroupement des reservations
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES
(60.00, 10);

-- -----------------------------
-- LIEUX (VARIABLES)
-- -----------------------------
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aeroport Ivato', 'AEROPORT'),
('H01', 'Hotel Ibis', 'HOTEL'),
('H02', 'Hotel Alpha', 'HOTEL'),
('H03', 'Hotel Beta', 'HOTEL'),
('H04', 'Hotel Carlton', 'HOTEL');

-- -----------------------------
-- DISTANCES (VARIABLES)
-- -----------------------------
INSERT INTO distance (from_id, to_id, km) VALUES
(1,2,10.0), (1,3,12.0), (1,4,12.0), (1,5,18.0),
(2,1,10.0), (3,1,12.0), (4,1,12.0), (5,1,18.0),
(2,3,6.0),  (3,2,6.0),
(2,4,8.0),  (4,2,8.0),
(2,5,12.0), (5,2,12.0),
(3,4,7.0),  (4,3,7.0),
(3,5,9.0),  (5,3,9.0),
(4,5,5.0),  (5,4,5.0);

-- -----------------------------
-- VEHICULES (VARIABLES)
-- -----------------------------
INSERT INTO vehicule (immatriculation, capacite, carburant, heure_disponibilite) VALUES
('V1-DIESEL-4',   4,  'D', '00:00:00'),
('V2-ESSENCE-4',  4,  'E', '00:00:00'),
('V3-DIESEL-6',   6,  'D', '00:00:00'),
('V4-ESSENCE-8',  8,  'E', '06:00:00'),
('V5-DIESEL-12', 12,  'D', '00:00:00');

-- -----------------------------
-- RESERVATIONS (VARIABLES)
-- -----------------------------
-- Date de test recommandee: 2026-03-20
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-01', 7,  '2026-03-19 09:00:00', 2),
('Client-02', 20,  '2026-03-19 09:00:00', 3),
('Client-03', 3,  '2026-03-19 09:10:00', 2),
('Client-04', 10,  '2026-03-19 09:15:00', 2),
('Client-05', 5,  '2026-03-19 09:20:00', 2),
('Client-06', 12,  '2026-03-19 13:30:00', 2);

-- -----------------------------
-- REQUETES D'ANALYSE
-- -----------------------------
-- Detail affectations (apres execution de l'affectation)
-- SELECT
--   a.id,
--   r.id AS reservation_id,
--   r.id_client,
--   r.nombre_passagers AS reservation_passagers,
--   a.nombre_passagers_affectes,
--   v.immatriculation,
--   to_char(a.date_heure_depart, 'YYYY-MM-DD HH24:MI') AS depart,
--   to_char(a.date_heure_retour, 'YYYY-MM-DD HH24:MI') AS retour,
--   a.ordre_livraison
-- FROM affectation a
-- JOIN reservation r ON r.id = a.id_reservation
-- JOIN vehicule v ON v.id = a.id_vehicule
-- ORDER BY a.date_heure_depart, v.immatriculation, r.id;

-- Reservations non affectees
-- SELECT r.*
-- FROM reservation r
-- LEFT JOIN affectation a ON a.id_reservation = r.id
-- WHERE a.id IS NULL
-- ORDER BY r.date_heure_depart;
