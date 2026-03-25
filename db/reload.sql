INSERT INTO parametre (vitesse_moyenne, temps_attente) VALUES (50.00, 30);
INSERT INTO lieu (code, libelle, type) VALUES ('AER', 'aeroport', 'AEROPORT'), ('H01', 'hotel1', 'HOTEL'), ('H02', 'hotel2', 'HOTEL');
INSERT INTO distance (from_id, to_id, km) VALUES (5, 6, 90.0), (5, 7, 35.0), (6, 7, 60.0), (6, 5, 90.0), (7, 5, 35.0), (7, 6, 60.0);
INSERT INTO vehicule (immatriculation, capacite, carburant, heure_disponibilite) VALUES ('vehicule1', 5, 'D', '00:00:00'), ('vehicule2', 5, 'E', '00:00:00'), ('vehicule3', 12, 'D', '00:00:00'), ('vehicule4', 9, 'D', '00:00:00'), ('vehicule5', 12, 'E', '13:00:00');
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES ('Client1', 7, '2026-03-19 09:00:00', 6), ('Client2', 20, '2026-03-19 08:00:00', 7), ('Client3', 3, '2026-03-19 09:10:00', 6), ('Client4', 10, '2026-03-19 09:15:00', 6), ('Client5', 5, '2026-03-19 09:20:00', 6), ('Client6', 12, '2026-03-19 13:30:00', 6);
