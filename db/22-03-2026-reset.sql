-- ==================================================
-- REINITIALISATION AVEC DONNEES PAR DEFAUT
-- Base cible: operateur
-- Objectif: remettre un jeu minimal stable pour demarrage
-- ==================================================

\c operateur;

-- Nettoyage complet des donnees metier
TRUNCATE TABLE affectation RESTART IDENTITY CASCADE;
TRUNCATE TABLE reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE lieu RESTART IDENTITY CASCADE;
TRUNCATE TABLE parametre RESTART IDENTITY CASCADE;

ALTER TABLE vehicule ADD COLUMN heure_disponibilite TIME NOT NULL DEFAULT '00:00:00';

-- Parametres par defaut
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES
(50.00, 30);

-- Lieux par defaut
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aeroport Ivato', 'AEROPORT'),
('hotel1', 'Hotel 1', 'HOTEL'),
('hotel2', 'Hotel 2', 'HOTEL');

-- Distances par defaut
INSERT INTO distance (from_id, to_id, km) VALUES
(1,2,90.0), (2,1,90.0),
(1,3,35.0), (3,1,35.0),
(2,3,60.0),  (3,2,60.0);

-- Vehicules par defaut
INSERT INTO vehicule (immatriculation, capacite, carburant, heure_disponibilite) VALUES
('Vehicule 1', 5,  'D', '09:00:00'),
('Vehicule 2', 5,  'E', '09:00:00'),
('Vehicule 3', 12,  'D', '00:00:00'),
('Vehicule 4', 9,  'D', '09:00:00'),
('Vehicule 5',12, 'E', '13:00:00');

-- Reservations par defaut (petit scenario lisible)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('Client-01', 7,  '2026-03-19 09:00:00', 2),
('Client-02', 20,  '2026-03-19 09:00:00', 3),
('Client-03', 3,  '2026-03-19 09:10:00', 2),
('Client-04', 10,  '2026-03-19 09:15:00', 2),
('Client-05', 5,  '2026-03-19 09:20:00', 2),
('Client-06', 12,  '2026-03-19 13:30:00', 2);

-- Note:
-- - DEF-C04 peut etre decoupee selon disponibilite/capacites des vehicules.
-- - Lancer ensuite /affectation/affecter?date=2026-03-21
