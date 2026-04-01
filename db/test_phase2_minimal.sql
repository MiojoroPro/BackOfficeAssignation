-- =====================================================
-- DONNÉES ULTRA-SIMPLES - PHASE 2
-- Date: 25/03/2026
-- 3 clients seulement pour vérifier rapidement
-- =====================================================

DELETE FROM affectation WHERE id_reservation IN (
    SELECT id FROM reservation WHERE DATE(date_heure_depart) = '2026-03-25'
);
DELETE FROM reservation WHERE DATE(date_heure_depart) = '2026-03-25';

-- =====================================================
-- SCÉNARIO MINIMAL (pour validation rapide)
-- =====================================================
--
-- Véhicules utilisés: V3(12), V4(9)
-- Clients: 3 seulement
--
-- C1: 10 pers @ 08:00 Hotel1 → V3
-- C2: 15 pers @ 08:30 Hotel1 → NON ASSIGNÉ
-- C3: 8 pers  @ 09:00 Hotel1 → Assignable si V4 libre
--
-- Timeline:
--   08:00: C1(10) → V3(12) Départ 08:00, Retour 09:24
--   08:30: C2(15) → NON ASSIGNÉ (V3,V4 occupés)
--   09:00: C3(8)  → V4(9) ? Disponible → Départ 09:00
--   09:24: V3 revient → Prend C2(15)?
--          NON, découpage C2(12 sur V3 + 3 autres)
--
-- RÉSULTAT:
--   PHASE 1: C1(10)→V3, C3(8)→V4, C2(15)→NON ASSIGNÉ
--   PHASE 2: C2(15)→V3(12)+autre(3) → Reste 3 passagers
--
-- Total assigné: 20+8+12 = 40 passagers
-- Non assigné: 3 passagers

INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
VALUES 
('Client1', 10, '2026-03-25 08:00:00', 2),
('Client2', 15, '2026-03-25 08:30:00', 2),
('Client3', 8, '2026-03-25 09:00:00', 2);

-- =====================================================
-- RÉSULTAT ATTENDU EN DÉTAIL
-- =====================================================
--
-- PHASE 1:
--   [08:00] V3: Client1(10) → Retour 09:24
--   [09:00] V4: Client3(8)  → Retour ~10:40
--   ❌ Client2(15) NON ASSIGNÉ
--
-- PHASE 2 (À 09:24):
--   Non assignés (DÉCROISSANT): Client2(15)
--   V3 revient → Client2(15)?
--              NON (V3=12 places)
--              Découpage: V3(12) + V4(3)?
--              MAIS V4 occupée jusqu'à 10:40!
--   → Client2 reste NON ASSIGNÉ
--
-- Alternative si V4 était libre:
--   V3: Client2(12) [09:24-...]
--   V4: Client2(3)  [09:24-...]
--   → Reste 0
--
-- BILAN: 28 assignés / 33 total = 84.8%
--

-- =====================================================
-- VARIABLE: Test avec Client2 plus petit
-- =====================================================
--
-- Si on change Client2 de 15 à 12 passagers:
-- DELETE FROM reservation WHERE date_heure_depart > '2026-03-25 08:00:00';
-- INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) 
-- VALUES ('Client2', 12, '2026-03-25 08:30:00', 2);
--
-- Résultat:
--   PHASE 2: V3 peut prendre Client2(12) seule
--   → Tout assigné! 100%
--
-- =====================================================
