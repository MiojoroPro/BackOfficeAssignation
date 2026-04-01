-- =====================================================
-- DONNÉES DE TEST - PHASE 2 (Gestion des non assignés)
-- Date: 20/03/2026
-- Scénario: Illustration de la réutilisation de véhicules
-- =====================================================

-- Nettoyer les affectations existantes
DELETE FROM affectation WHERE id_reservation IN (
    SELECT id FROM reservation WHERE DATE(date_heure_depart) = '2026-03-20'
);
DELETE FROM reservation WHERE DATE(date_heure_depart) = '2026-03-20';

-- =====================================================
-- VUE D'ENSEMBLE DU SCÉNARIO
-- =====================================================
-- Véhicules (mêmes que avant):
--   V1: 5 places, Diesel
--   V2: 5 places, Essence
--   V3: 12 places, Diesel
--   V4: 9 places, Diesel
--   V5: 12 places, Essence (dispo 13:00)
-- 
-- PHASE 1 résultat attendu:
--   Groupe 1 (08:00): Client2 (20 pers) → Découpage V3(12) + V4(8)
--   Groupe 2 (09:00-09:20): Clients 1,3,4,5 → Assignés à V1,V2,V3,V4
--   Groupe 3 (10:00-10:30): Client6 (15 pers) → NON ASSIGNÉ (aucun véhicule)
--   Groupe 4 (11:00-11:15): Client7 (10 pers) → NON ASSIGNÉ
-- 
-- PHASE 2 résultat attendu:
--   V3 revient à 11:12 → Prend Client6 (15 pers, priorité DÉCROISSANT)
--   V4 revient à 11:12 → Prend Client7 (10 pers, aucun découpage possible)
-- =====================================================

-- =====================================================
-- INSERTIONS
-- =====================================================

-- Groupe 1 (08:00) - Client volumineux: décopuage en 2 véhicules
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES ('Client2', 20, '2026-03-20 08:00:00', 3);

-- Groupe 2 (09:00-09:20) - Clients normaux, assignables
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES 
('Client1', 7, '2026-03-20 09:00:00', 2),
('Client3', 3, '2026-03-20 09:10:00', 2),
('Client4', 10, '2026-03-20 09:15:00', 2),
('Client5', 5, '2026-03-20 09:20:00', 2);

-- Groupe 3 (10:00-10:30) - Client NON ASSIGNÉ (trop volumineux, aucun véhicule dispo)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES ('Client6', 15, '2026-03-20 10:00:00', 2);

-- Groupe 4 (11:00-11:15) - Client NON ASSIGNÉ (trop volumineux pour V1/V2)
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES ('Client7', 10, '2026-03-20 11:00:00', 2);

-- Client supplémentaire très volumineux pour tester découpage en PHASE 2
INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES ('Client8', 25, '2026-03-20 11:30:00', 2);

-- =====================================================
-- RÉSULTAT ATTENDU
-- =====================================================
-- 
-- PHASE 1 (Affectation normale):
-- ===========================
-- 1. Client2 (20 pers, 08:00 → hotel2)
--    Découpage: V3(12) départ 08:00-09:24, V4(8) départ 08:00-09:24
-- 
-- 2. Client1 (7 pers, 09:00 → hotel1)
--    V1 (5 places) départ 09:24-11:12 [combiné avec ?]
--    V2 (5 places) départ 09:24-11:12 [combiné avec Client3 (3 pers)]
-- 
-- 3. Client4 (10 pers, 09:15 → hotel1)
--    V3 (déjà occupée par Client2) → Reutilisée départ 09:24-11:12
-- 
-- 4. Client5 (5 pers, 09:20 → hotel1)
--    V1 ou V2 (l'une des deux) départ 09:24-11:12
--
-- 5. Client6 (15 pers, 10:00 → hotel1) 
--    ❌ NON ASSIGNÉ: Aucun véhicule capable et libre à ce moment
-- 
-- 6. Client7 (10 pers, 11:00 → hotel1)
--    ❌ NON ASSIGNÉ: Tous les véhicules occupés
--
-- 7. Client8 (25 pers, 11:30 → hotel1)
--    ❌ NON ASSIGNÉ: Trop volumineux pour être complètement assigné
--
-- PHASE 2 (Réutilisation avec non assignés):
-- ==========================================
-- 
-- V4 revient à 09:24
--   Non assignés disponibles (tri DÉCROISSANT):
--     1. Client8 (25) - Trop volumineux pour V4(9)
--     2. Client6 (15) - Trop volumineux pour V4(9)
--     3. Client7 (10) - Trop pour V4(9)
--   → V4 reste libre
--
-- V3 revient à 09:24 (mais occupée par Client4 jusqu'à 11:12)
-- V1 revient à 11:12
--   Non assignés disponibles (tri DÉCROISSANT):
--     1. Client8 (25) - Trop pour V1(5)
--     2. Client6 (15) - Trop pour V1(5)
--     3. Client7 (10) - Trop pour V1(5)
--   → V1 ne peut pas les prendre
--
-- V4 revient à 11:12
--   Non assignés disponibles (tri DÉCROISSANT):
--     1. Client8 (25) - Trop pour V4(9)
--     2. Client6 (15) - Trop pour V4(9)
--     3. Client7 (10) - Trop pour V4(9)
--   → V4 ne peut pas les prendre
--
-- V3 revient à 11:12
--   Non assignés disponibles (tri DÉCROISSANT):
--     1. Client8 (25) - Trop pour V3(12 places)
--        Découpage: V3(12) + V5(13) ou autre
--          MAIS V5 dispo à 13:00 seulement
--          → Client8 attend V5
--     2. Client6 (15) - Trop pour V3(12)
--        Découpage: V3(12) + V5(3) possible !
--     3. Client7 (10) - OK pour V3(12)
--
--   Affectation Client6 (15 pers):
--     V3 (12 places) départ 11:12-? 
--     + reste (3 pers) attend V5
--
-- V5 revient à 13:00
--   Non assignés restants:
--     1. Client8 (25 pers) - Découpage: V5(12) + autre?
--     2. Client7 (10 pers) - OK pour V5(12)
--
--   → Client7 prend V5 départ 13:00
--   → Client8 prend V5 découpage (12) + ...
--
-- =====================================================

-- STATISTIQUES:
-- =============
-- Véhicules: 5
-- Réservations: 7
-- Non assignés attendus: 3 (Client6, Client7, Client8)
-- Taux d'assignation PHASE 1: 4/7 = 57%
-- Taux d'assignation PHASE 2: 1-2/3 = 33-66% (dépend découpage V5)
-- Taux final: 6-7/7 = 85-100%
