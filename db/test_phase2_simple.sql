-- =====================================================
-- DONNÉES SIMPLIFIÉES - PHASE 2
-- Date: 15/03/2026
-- Scénario simple pour tester réutilisation véhicules
-- =====================================================

DELETE FROM affectation WHERE id_reservation IN (
    SELECT id FROM reservation WHERE DATE(date_heure_depart) = '2026-03-15'
);
DELETE FROM reservation WHERE DATE(date_heure_depart) = '2026-03-15';

-- =====================================================
-- SCÉNARIO SIMPLIFIÉ
-- =====================================================
-- 
-- Objectif: Voir clairement les non-assignés réutilisés
--
-- 3 véhicules seulement:
--   V1: 5 places
--   V3: 12 places (les plus importantes)
--   V4: 9 places
--
-- 4 clients:
--   C1: 8 pers (sera découpe V3=5 + V4=3, ou découpage)
--   C2: 12 pers (assigné à V3)
--   C3: 10 pers (NON ASSIGNÉ, car V3 et V4 occupés après Client2)
--   C4: 5 pers (assignable à V1 si libre)
--
-- Timeline:
--   08:00: Client1(8) → V3(5) + V4(3) 
--          Peu: Aero->Hotel1 42+42=84min → Retour 09:24
--
--   08:30: Client2(12) → Non assignable (V3,V4 occupés)
--          → Reste client normal à assigner plus tard
--          
--   09:00: Client3(10) → V1(5) ? Non (10 > 5)
--          → V3 ? Occupée jusqu'à 09:24
--          → V4 ? Occupée jusqu'à 09:24
--          → NON ASSIGNÉ
--
--   09:24: V3, V4 reviennent
--          Client2(12) → V3(12) départ 09:24
--          Client3(10) → ??? (en file d'attente)
--                        V4(9 places)? Peut prendre 9 de Client3
--                        Reste 1 de Client3 → Non assigné finale
--

INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES 
('Client1', 8, '2026-03-15 08:00:00', 2),
('Client2', 12, '2026-03-15 08:30:00', 2),
('Client3', 10, '2026-03-15 09:00:00', 2),
('Client4', 5, '2026-03-15 09:10:00', 2);

-- =====================================================
-- RÉSULTAT ATTENDU
-- =====================================================
--
-- PHASE 1:
-- --------
-- V3: Client1 (5 pers) - Départ 08:00, Retour 09:24
-- V4: Client1 (3 pers) - Départ 08:00, Retour 09:24
-- V1: Client4 (5 pers) - Départ 09:10?, Retour ?
-- 
-- Non assignés après PHASE 1:
--   - Client2 (12 pers)
--   - Client3 (10 pers)
--
-- PHASE 2:
-- --------
-- À 09:24, V3 et V4 reviennent
--
-- Non assignés (DÉCROISSANT): Client2(12) > Client3(10)
--
-- V3 (12 places) libre:
--   → Prend Client2(12)
--      Départ: 09:24, Retour: 09:24 + 84min = 10:48
--   → Reste Client3(10)
--
-- V4 (9 places) libre:
--   → Prend 9 de Client3(10)
--      Départ: 09:24, Retour: ?
--   → Reste 1 de Client3 non assigné
--
-- Résultat Final:
-- ===============
-- Affectations totales:
--   V3: Client1(5) [08:00-09:24] + Client2(12) [09:24-10:48]
--   V4: Client1(3) [08:00-09:24] + Client3(9) [09:24-?]
--   V1: Client4(5) [09:10-?]
--
-- Non assignés finaux:
--   Client3 partiellement: 1 passager restant
--
-- Taux de succès: 4 clients, 39 passagers assignés = 100%
--

-- =====================================================
-- VERSION ALTERNATIVE: Plus simple encore
-- =====================================================
-- Si on veut juste 2 clients non assignés clairs:

-- DELETE FROM reservation WHERE id > 999;
-- INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
-- VALUES 
-- ('Client1', 6, '2026-03-15 08:00:00', 2),  -- V3
-- ('Client2', 15, '2026-03-15 08:30:00', 2), -- NON ASSIGNÉ en PHASE 1
-- ('Client3', 10, '2026-03-15 09:00:00', 2); -- NON ASSIGNÉ en PHASE 1
-- 
-- PHASE 1: V3 prend Client1(6) → Retour 09:24
-- Client2(15) et Client3(10) sont NON ASSIGNÉS
--
-- PHASE 2: À 09:24
-- - V3 revient → Client2(15)? Non (V3 = 12 places)
-- - Découpage: V3(12) + V4(3)? Client2 = 15, donc Client2(12) sur V3 + Client2(3) sur V4
-- - Reste Client3(10) qui peut prendre V4(9) → Client3(9) + 1 passager non assigné
--
-- Résultat Final:
--   ✓ Client1: 6 (V3)
--   ✓ Client2: 15 (V3(12) + V4(3))
--   ~ Client3: 9 (V4) + 1 non assigné
--
-- Non assignés définitifs: 1 passager de Client3
