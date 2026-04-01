# 🧪 Comparatif des 3 Fichiers de Test PHASE 2

## 📊 Vue d'ensemble

| Aspect | **Minimal** | **Simple** | **Complet** |
|--------|-----------|----------|-----------|
| **Fichier** | `test_phase2_minimal.sql` | `test_phase2_simple.sql` | `test_phase2.sql` |
| **Date** | 25/03/2026 | 15/03/2026 | 20/03/2026 |
| **Nb Clients** | 3 | 4 | 7-8 |
| **Découpage** | Non | Oui (PHASE 1) | Oui (PHASE 1 & 2) |
| **Non-assignés** | 1 | 2 | 3+ |
| **Complexité** | ⭐ | ⭐⭐ | ⭐⭐⭐ |
| **Temps test** | ~2 min | ~5 min | ~10 min |
| **Idéal pour** | Vérification rapide | Validation PHASE 2 | Test exhaustif |

---

## 📋 Détails de chaque approche

### 🔵 Test MINIMAL (25/03/2026)
**Use Case**: Vérification rapide en 2 minutes

```
Réservations:
  Client1: 10 pers @ 08:00 → V3 [08:00-09:24]
  Client2: 15 pers @ 08:30 → ❌ NON ASSIGNÉ
  Client3: 8 pers  @ 09:00 → V4 [09:00-...]

PHASE 1: 18 assignés, 15 non assignés
PHASE 2: Client2(15) trop gros pour V3(12) seule → Reste non assigné

Résultat: 18/33 = 54.5% (illustration d'impasse)
```

**Bonnes pratiques**:
- ✓ Rapide à exécuter
- ✓ Teste le tri DÉCROISSANT
- ✓ Montre un cas où PHASE 2 ne suffit pas
- ✗ Complexité minimale

**Commande**:
```bash
psql -U postgres -d operateur -f db/test_phase2_minimal.sql
```

---

### 🟠 Test SIMPLE (15/03/2026)
**Use Case**: Validation principal de PHASE 2

```
Réservations:
  Client1: 8 pers  @ 08:00 → Découpage V3(5) + V4(3) [08:00-09:24]
  Client2: 12 pers @ 08:30 → ❌ NON ASSIGNÉ
  Client3: 10 pers @ 09:00 → ❌ NON ASSIGNÉ  
  Client4: 5 pers  @ 09:10 → V1 [09:10-...]

PHASE 1: 13 assignés, 22 non assignés
PHASE 2: 
  - V3 revient @ 09:24 → Client2(12) ✓
  - V4 revient @ 09:24 → Client3(9/10) ✓
  - Reste: 1 passager

Résultat: 31/35 = 88.6% (succès PHASE 2)
```

**Bonnes pratiques**:
- ✓ Tests découpage PHASE 1
- ✓ Tests réutilisation PHASE 2
- ✓ Assez simple à analyser
- ✓ **RECOMMANDÉ pour démarrer**
- ✗ Pas de complexité extrême

**Commande**:
```bash
psql -U postgres -d operateur -f db/test_phase2_simple.sql
```

**Checklist de validation**:
- [ ] V3 retour 09:24 prend Client2(12)
- [ ] V4 retour 09:24 prend Client3(9)
- [ ] 1 passager Client3 non assigné final
- [ ] Ordre DÉCROISSANT: Client2 avant Client3

---

### 🔴 Test COMPLET (20/03/2026)
**Use Case**: Validation exhaustive tous les cas

```
Réservations:
  Client1: 7 pers  @ 09:00 → Assignable
  Client2: 20 pers @ 08:00 → Découpage V3(12) + V4(8)
  Client3: 3 pers  @ 09:10 → Assignable
  Client4: 10 pers @ 09:15 → Assignable
  Client5: 5 pers  @ 09:20 → Assignable
  Client6: 15 pers @ 10:00 → ❌ NON ASSIGNÉ
  Client7: 10 pers @ 11:00 → ❌ NON ASSIGNÉ
  Client8: 25 pers @ 11:30 → ❌ NON ASSIGNÉ

PHASE 1: 30-32 assignés, 48-50 non assignés
PHASE 2: Réutilisation complexe avec ordre DÉCROISSANT
         - Client8(25) > Client6(15) > Client7(10)
         - Découpage multi-véhicule possible

Résultat: Dépend du découpage (85-100%)
```

**Bonnes pratiques**:
- ✓ Tests TOUS les cas d'usage
- ✓ Découpage PHASE 1 + PHASE 2
- ✓ Plusieurs non-assignés
- ✓ Priorité DÉCROISSANT complexe
- ✗ Difficile à analyser en détail
- ✗ Plus long à exécuter

**Commande**:
```bash
psql -U postgres -d operateur -f db/test_phase2.sql
```

**Checklist de validation**:
- [ ] Client2 découpage PHASE 1 ✓
- [ ] Non-assignés triés DESC ✓
- [ ] Réutilisation multiples véhicules ✓
- [ ] Découpage potentiel PHASE 2 ✓

---

## 🎯 Recommandation

### Pour débuter (Jour 1):
```bash
# Simple et rapide
psql -U postgres -d operateur -f db/test_phase2_minimal.sql
# Puis vérifier l'affectation via l'API ou l'interface GUI
```

### Pour valider PHASE 2 (Jour 2):
```bash
# Test recommandé - bon équilibre
psql -U postgres -d operateur -f db/test_phase2_simple.sql
# Vérifier tous les critères de la checklist
```

### Pour validation final (Jour 3):
```bash
# Test exhaustif
psql -U postgres -d operateur -f db/test_phase2.sql
# Valider tous les cas extrêmes
```

---

## 🔍 Comment analyser les résultats

### Observation 1: Ordre de départ
```sql
SELECT 
  a.date_heure_depart,
  v.immatriculation,
  r.id_client,
  a.nombre_passagers_affectes
FROM affectation a
JOIN vehicule v ON a.id_vehicule = v.id
JOIN reservation r ON a.id_reservation = r.id
ORDER BY a.date_heure_depart;
```

**Attendu pour test_phase2_simple** :
```
Heure       Véhicule  Client   Passagers
-----------+----------+-------+-----------
09:24      vehicule3 Client2   12
09:24      vehicule4 Client3   9
(autres véhicules avec heure différente)
```

### Observation 2: Non-assignés finaux
```sql
SELECT r.id_client, SUM(a.nombre_passagers_affectes) as assignes, r.nombre_passagers
FROM reservation r
LEFT JOIN affectation a ON r.id = a.id_reservation
WHERE DATE(r.date_heure_depart) = '2026-03-15'
GROUP BY r.id, r.nombre_passagers
HAVING SUM(a.nombre_passagers_affectes) < r.nombre_passagers 
   OR SUM(a.nombre_passagers_affectes) IS NULL;
```

**Attendu pour test_phase2_simple** :
```
Client    Assignes  Total  Reste
---------+----------+-------+------
Client3   9         10     1
```

---

## 📝 Notes importantes

1. **Les durées changent selon les destinations** :
   - Hotel1: 90 km = 216 min aller-retour
   - Hotel2: 35 km = 84 min aller-retour
   - Plan sur vos données!

2. **L'ordre DÉCROISSANT est CRITIQUE** :
   - Il faut que Client2(15) soit pris avant Client3(10)
   - Sinon la PHASE 2 n'est pas bien testée

3. **Temps d'attente n'est PAS appliqué en PHASE 2** :
   - Les non-assignés partent immédiatement au retour du véhicule
   - Contrairement à la PHASE 1

4. **Découpage peut échouer** :
   - Si aucun véhicule ne peut compléter le découpage
   - Example: Client(25) = V3(12) + V4(9) + V1(5) = 26, OK!
   - Mais V1 peut être occupée!

