CREATE DATABASE IF NOT EXISTS assignation;
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
INSERT INTO Hotel (nom, adresse) VALUES
('Hôtel du Centre', '10 Rue de la Paix, Antananarivo'),
('Hôtel Océan', '25 Avenue de la Mer, Toamasina'),
('Hôtel Montagne', '5 Rue des Sommets, Fianarantsoa');
