CREATE DATABASE assignation;
\c assignation;

CREATE TABLE Hotel(
    id_Hotel SERIAL PRIMARY KEY,    
    nom VARCHAR(255) NOT NULL,
    adresse VARCHAR(255) NOT NULL
);

CREATE TABLE Reservation(
    id SERIAL PRIMARY KEY,
    id_Client VARCHAR(255) NOT NULL,
    nbpassagers INT NOT NULL,
    dateheure TIMESTAMP NOT NULL,
    id_Hotel INT NOT NULL,
    FOREIGN KEY (id_Hotel) REFERENCES Hotel(id_Hotel)
);

-- Données de test pour les hôtels
INSERT INTO Hotel (id_Hotel, nom, adresse) VALUES
(1, 'Colbert', 'Adresse Colbert'),
(2, 'Novotel', 'Adresse Novotel'),
(3, 'Ibis', 'Adresse Ibis'),
(4, 'Lokanga', 'Adresse Lokanga');


INSERT INTO Reservation (id, id_Client, nbpassagers, dateheure, id_Hotel) VALUES
(1, '4631', 11, '2026-02-05 00:01:00', 3),
(2, '4394', 1,  '2026-02-05 23:55:00', 3),
(3, '8054', 2,  '2026-02-09 10:17:00', 1),
(4, '1432', 4,  '2026-02-01 15:25:00', 2),
(5, '7861', 4,  '2026-01-28 07:11:00', 1),
(6, '3308', 5,  '2026-01-28 07:45:00', 1),
(7, '4484', 13, '2026-02-28 08:25:00', 2),
(8, '9687', 8,  '2026-02-28 13:00:00', 2),
(9, '6302', 7,  '2026-02-15 13:00:00', 1),
(10,'8640', 1,  '2026-02-18 22:55:00', 4);