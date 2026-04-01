# 🧪 Données de Test - PHASE 2 - Guide Pratique

## 📌 Deux approches de test

### ✅ Approche 1: Test SIMPLIFIÉ (recommandé pour démarrer)
**Fichier**: `db/test_phase2_simple.sql`

**4 clients seulement**:
```
Client1: 8 pers   @ 08:00 → Assignable
Client2: 12 pers  @ 08:30 → NON ASSIGNÉ en PHASE 1
Client3: 10 pers  @ 09:00 → NON ASSIGNÉ en PHASE 1
Client4: 5 pers   @ 09:10 → Assignable
```

**Résultat attendu** :
```
PHASE 1:
  ✓ V3: Client1(5) [08:00-09:24]
  ✓ V4: Client1(3) [08:00-09:24]
  ✓ V1: Client4(5) [09:10-?]
  ❌ Client2(12) NON ASSIGNÉ
  ❌ Client3(10) NON ASSIGNÉ

PHASE 2 (À 09:24, V3 et V4 reviennent):
  ✓ V3: Client2(12) [09:24-10:48] (tri DESC: Client2 > Client3)
  ✓ V4: Client3(9) [09:24-?] (9 de 10)
  ❌ Client3 partiellement: 1 passager reste

BILAN: 7/8 passagers assignés ✓
```

---

### 🔬 Approche 2: Test COMPLET (pour validation exhaustive)
**Fichier**: `db/test_phase2.sql`

**7-8 clients variés** avec plusieurs niveaux de complexité :
- Découpage immédiat (PHASE 1)
- Clients normaux assignés
- Clients non assignés de différentes tailles
- Clients très volumineux nécessitant découpage en PHASE 2

---

## 🚀 Instructions d'exécution

### Étape 1: Charger les données
```bash
cd E:\S5\Projet-Naina\BackOfficeAssignation\db

# Option A: Test simple (RECOMMANDÉ)
psql -U postgres -d operateur -f test_phase2_simple.sql

# Option B: Test complet
psql -U postgres -d operateur -f test_phase2.sql
```

### Étape 2: Vérifier dans la base
```sql
-- Voir les réservations chargées
SELECT id, id_client, nombre_passagers, DATE(date_heure_depart), id_lieu_destination 
FROM reservation 
WHERE DATE(date_heure_depart) = '2026-03-15'
ORDER BY date_heure_depart;

-- Avant affectation (vérification):
SELECT COUNT(*) FROM affectation WHERE id_reservation IN (
  SELECT id FROM reservation WHERE DATE(date_heure_depart) = '2026-03-15'
);
-- Doit retourner: 0
```

### Étape 3: Exécuter l'affectation
Accédez à l'API ou l'interface web:

```bash
# API REST (avec TOKEN du jour)
curl -X POST "http://localhost:8383/reservation/api/affectations?date=2026-03-15" \
     -H "Authorization: Bearer mm6kaXBsJTMdqbg96LXsNwhqxI5OJ3WQbL_fMYFGbyM"
```

Ou via l'interface web:
```
http://localhost:8383/reservation/reservations/new
→ Selectionnez la date 15/03/2026
→ Cliquez "Générer affectations"
```

### Étape 4: Vérifier les résultats

```sql
-- Voir toutes les affectations créées (ordered par heure de départ)
SELECT 
  a.id_affectation,
  v.immatriculation,
  r.id_client,
  a.nombre_passagers_affectes,
  a.date_heure_depart,
  a.date_heure_retour,
  (a.date_heure_retour - a.date_heure_depart) as duree
FROM affectation a
JOIN vehicule v ON a.id_vehicule = v.id
JOIN reservation r ON a.id_reservation = r.id
WHERE DATE(a.date_heure_depart) = '2026-03-15'
ORDER BY a.date_heure_depart, v.immatriculation;

-- Voir les clients non assignés finaux
SELECT r.id_client, r.nombre_passagers, r.date_heure_depart
FROM reservation r
LEFT JOIN affectation a ON r.id = a.id_reservation
WHERE DATE(r.date_heure_depart) = '2026-03-15'
AND a.id_affectation IS NULL;
```

---

## 📊 Checklist de Validation

Pour le test simple (`test_phase2_simple.sql`), vérifier :

- [ ] **PHASE 1 - Affectations initiales**
  - [ ] Client1(8): Découpage en V3(5) + V4(3) ✓
  - [ ] Client4(5): Assigné à V1 ✓
  - [ ] Client2(12): NON ASSIGNÉ ✓
  - [ ] Client3(10): NON ASSIGNÉ ✓

- [ ] **PHASE 2 - Réutilisation**
  - [ ] À 09:24, V3 et V4 reviennent (ordre DESC respected)
  - [ ] V3 prend Client2(12) [priorité DESC] ✓
  - [ ] V4 prend Client3(9) ✓
  - [ ] 1 passager de Client3 reste non assigné ✓

- [ ] **Horaires**
  - [ ] V3 retour PHASE 1: 09:24 ✓
  - [ ] V3 départ PHASE 2: 09:24 (immédiat) ✓
  - [ ] V3 retour PHASE 2: 10:48 (09:24 + 84 min) ✓

- [ ] **Total final**
  - [ ] 7 passagers assignés / 8 total = 87.5% ✓
  - [ ] Client3 partiellement assigné (9/10) ✓

---

## 🐛 Troubleshooting

**Problème**: Les clients non assignés ne sont pas réutilisés
- → Vérifier que `affecterClientsNonAssignes()` est bien appelée
- → Vérifier les logs de la PHASE 2

**Problème**: Ordre des véhicules incorrect
- → Vérifier le tri DÉCROISSANT dans `affecterClientsNonAssignes()`
- → L'ordre doit être: Client2(12) avant Client3(10)

**Problème**: Affectations nulles
- → Vérifier la BD PostgreSQL est connectée
- → Vérifier `reload.sql` pour les paramètres (vitesse, temps_attente)

---

## 📚 Fichiers de référence

- **Code**: `src/service/AffectationService.java` (méthode `affecterClientsNonAssignes`)
- **Documentation**: `TEST_PHASE2_DOCUMENTATION.md`
- **Données**: `db/test_phase2_simple.sql` ou `db/test_phase2.sql`

