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

-- Parametres par defaut
INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES
(60.00, 30);

-- Lieux par defaut
INSERT INTO lieu (code, libelle, type) VALUES
('AER', 'Aeroport Ivato', 'AEROPORT'),
('H01', 'Hotel Ibis', 'HOTEL'),
('H02', 'Hotel Carlton', 'HOTEL'),
('H03', 'Hotel Colbert', 'HOTEL');

-- Distances par defaut
INSERT INTO distance (from_id, to_id, km) VALUES
(1,2,10.0), (2,1,10.0),
(1,3,14.0), (3,1,14.0),
(1,4,12.0), (4,1,12.0),
(2,3,6.0),  (3,2,6.0),
(2,4,5.0),  (4,2,5.0),
(3,4,4.0),  (4,3,4.0);

-- Vehicules par defaut
INSERT INTO vehicule (immatriculation, capacite, carburant) VALUES
('DEF-001-D4', 4,  'D'),
('DEF-002-E4', 4,  'E'),
('DEF-003-D6', 6,  'D'),
('DEF-004-E8', 8,  'E'),
('DEF-005-D12',12, 'D');

-- Reservations par defaut (petit scenario lisible)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES
('DEF-C01', 4, '2026-03-21 08:00:00', 2),
('DEF-C02', 3, '2026-03-21 08:08:00', 3),
('DEF-C03', 2, '2026-03-21 09:15:00', 4),
('DEF-C04', 9, '2026-03-21 10:30:00', 2);

-- Note:
-- - DEF-C04 peut etre decoupee selon disponibilite/capacites des vehicules.
-- - Lancer ensuite /affectation/affecter?date=2026-03-21
