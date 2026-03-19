-- ==================================================
-- Migration: support decoupage de reservation
-- Objectif:
-- 1) Autoriser plusieurs affectations pour une meme reservation
-- 2) Stocker le nombre de passagers affectes par ligne
-- ==================================================

-- 1) Ajouter la colonne nombre_passagers_affectes si absente
ALTER TABLE affectation
ADD COLUMN IF NOT EXISTS nombre_passagers_affectes INT;

-- 2) Initialiser les anciennes lignes (1 affectation = toute la reservation)
UPDATE affectation a
SET nombre_passagers_affectes = r.nombre_passagers
FROM reservation r
WHERE a.id_reservation = r.id
  AND a.nombre_passagers_affectes IS NULL;

-- 3) Rendre la colonne obligatoire
ALTER TABLE affectation
ALTER COLUMN nombre_passagers_affectes SET NOT NULL;

-- 4) Supprimer la contrainte unique sur id_reservation (nom variable selon base)
DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT tc.constraint_name
      INTO constraint_name
    FROM information_schema.table_constraints tc
    JOIN information_schema.constraint_column_usage ccu
      ON tc.constraint_name = ccu.constraint_name
     AND tc.table_schema = ccu.table_schema
    WHERE tc.table_schema = 'public'
      AND tc.table_name = 'affectation'
      AND tc.constraint_type = 'UNIQUE'
      AND ccu.column_name = 'id_reservation'
    LIMIT 1;

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE affectation DROP CONSTRAINT %I', constraint_name);
    END IF;
END $$;
