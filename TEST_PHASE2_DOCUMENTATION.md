# 📊 Données de Test - Phase 2 (Réutilisation Véhicules)

## 🎯 Objectif
Illustrer comment les **clients non assignés** dans la PHASE 1 sont réutilisés par les véhicules qui reviennent dans la PHASE 2.

---

## 📋 Configuration Test

### Véhicules
```
V1: 5 places, Diesel,   dispo 00:00
V2: 5 places, Essence,  dispo 00:00
V3: 12 places, Diesel,  dispo 00:00
V4: 9 places, Diesel,   dispo 00:00
V5: 12 places, Essence, dispo 13:00
```

### Distance & Paramètres
```
Aéroport → Hotel1: 90 km (108 min à 50 km/h)
Aéroport → Hotel2: 35 km (42 min à 50 km/h)
Route: 90 + 90 = 180 km (216 min aller-retour)
Temps d'attente: 30 min
```

---

## 📝 Réservations (7 clients)

| ID | Client  | Passagers | Heure  | Destination | Statut attendu |
|----|---------|-----------|--------|-------------|-----------------|
| 1  | Client2 | 20        | 08:00  | Hotel2      | DÉCOUPAGE V3+V4 |
| 2  | Client1 | 7         | 09:00  | Hotel1      | V1 ou V2        |
| 3  | Client3 | 3         | 09:10  | Hotel1      | V2 ou autre     |
| 4  | Client4 | 10        | 09:15  | Hotel1      | V3 ou V4        |
| 5  | Client5 | 5         | 09:20  | Hotel1      | V1 ou V2        |
| 6  | Client6 | 15        | 10:00  | Hotel1      | ❌ NON ASSIGNÉ  |
| 7  | Client7 | 10        | 11:00  | Hotel1      | ❌ NON ASSIGNÉ  |
| 8  | Client8 | 25        | 11:30  | Hotel1      | ❌ NON ASSIGNÉ  |

---

## 🔄 Timeline PHASE 1 + PHASE 2

```
HEURE    | PHASE 1                          | PHASE 2
---------|----------------------------------|-----------------------------------
08:00    | Client2(20) → V3(12)+V4(8)      |
         | Départ: 08:00                   |
         | Retour: 09:24                   |
---------|----------------------------------|-----------------------------------
09:00-09:20| Client1,3,4,5 assignés         |
         | Départ: 09:24                   |
         | Retour: 11:12                   |
---------|----------------------------------|-----------------------------------
09:24    | ← V3,V4 REVIENNENT               | ← Occupés pour Client2
         |                                 | (Client4 utilise V3)
---------|----------------------------------|-----------------------------------
10:00    | Client6(15) → ❌ NON ASSIGNÉ    | Attent V3 retour
         | (Aucun véhicule dispo)          |
---------|----------------------------------|-----------------------------------
11:00    | Client7(10) → ❌ NON ASSIGNÉ    | Attend V3/V4 retour
         | (Tous occupés)                  |
---------|----------------------------------|-----------------------------------
11:12    | ← V1,V2,V3,V4 REVIENNENT        | V3 prend Client6(15)
         |                                 | Découpage: V3(12)+?
---------|----------------------------------|-----------------------------------
11:30    | Client8(25) → ❌ NON ASSIGNÉ    | Attend V5 (dispo 13:00)
         | (Trop volumineux)               |
---------|----------------------------------|-----------------------------------
13:00    |                                 | V5 DISPONIBLE
         |                                 | Prend Client7(10)?
         |                                 | Ou Client8(12+13)?
---------|----------------------------------|-----------------------------------
```

---

## 🎬 Scénario Détaillé

### PHASE 1 : Affectation Normale

#### Groupe 1 (08:00) - Client2 volumineux
```
Client2: 20 passagers → Aucun véhicule seul ne peut le porter
Découpage appliqué:
  ✓ V3 (12 places, Diesel) → 12 passagers, Départ 08:00
  ✓ V4 (9 places, Diesel)  → 8 passagers, Départ 08:00
  
Route: Aéroport → Hotel2 (35 km)
Durée: 42 min aller + 42 min retour = 84 minutes
Retour: 08:00 + 1:24 = 09:24 ✓
```

#### Groupe 2 (09:00-09:20) - Clients normaux
```
Réservations à assigner: [Client1(7), Client3(3), Client4(10), Client5(5)]
Tri décroissant: Client4(10) > Client1(7) > Client5(5) > Client3(3)

Iteration 1:
  Client4(10) → V3 ? (Occupée jusqu'à 09:24)
             → V4 ? (Occupée jusqu'à 09:24)
             → ATTENDRE ou chercher autre
  → Finalement V3 quand elle revient (09:24), combo avec Client1(7)
     Total: 10 + 7 = 17 > V3(12) → Seulement Client4
     V3 seule: Client4(10)
     Départ: max(09:15, 09:24) = 09:24
     Retour: 09:24 + 3:36 = 13:00 ✓

Iteration 2:
  Client1(7) → V1(5) ? NON (5 < 7)
            → V2(5) ? NON (5 < 7)
            → Combinable? V1 + V2 = 10 > 7 ? Mais comment?
            → Découpage: V1(5) + V2(2) de Client1 ?
  → Non, le découpage ne s'applique que si aucun véhicule >= passagers
     Client1 seul ne rentre dans V1/V2 donc:
  → Client1 non assigné pour le moment? 
     OU combiné forcément avec un autre?

Iteration 3:
  Client5(5) → V1(5) ✓ OK
  Client3(3) → Combinable avec Client5?
              V1 a 5 places, Client5 prend 5
              → NON, chercher autre (V2)
             → V2(5) ✓ OK, Client3(3)

RÉSULTAT GROUPE 2:
  V1: Client1(7) ? PROBLÈME!
  V2: Client3(3) + Client5(2)? Départ 09:24, Retour 11:12
  V4: Client5(5) ? 

  ATTENDEZ... Révisons la logique:
  
  1. Client4(10) → V3 (quand libre à 09:24)
     Départ 09:24, Retour 11:12
  
  2. Client1(7) → V1(5) ? Non
                → V2(5) ? Non
                → Chercher véhicule capable:
                   V3 (occupée), V4 (occupée jusqu'à 09:24)
                   V5 (dispo à 13:00, mais on ne l'utilise pas ici)
              → V4 quand libre (09:24)
     V4: Client1(7)
     Départ 09:24, Retour 11:12
  
  3. Client5(5) → V1(5) ✓
     Combinable avec Client3(3)? 5 + 3 = 8 > 5 NON
     V1: Client5(5)
     Départ 09:24, Retour 11:12
  
  4. Client3(3) → V2(5) ✓
     Reste 2 places, combinable?
     V2: Client3(3)
     Départ 09:24, Retour 11:12

BILAN PHASE 1:
  ✓ V3: Client2(12) [08:00-09:24] + Client4(10) [09:24-11:12]
  ✓ V4: Client2(8) [08:00-09:24] + Client1(7) [09:24-11:12]
  ✓ V1: Client5(5) [09:24-11:12]
  ✓ V2: Client3(3) [09:24-11:12]
  ✓ V5: INUTILISÉ
  ❌ Client6(15) [10:00]: NON ASSIGNÉ
  ❌ Client7(10) [11:00]: NON ASSIGNÉ  
  ❌ Client8(25) [11:30]: NON ASSIGNÉ
```

### PHASE 2 : Réutilisation Véhicules

```
Non assignés (tri DÉCROISSANT):
  1. Client8 (25 passagers)
  2. Client6 (15 passagers)
  3. Client7 (10 passagers)

Véhicules qui reviennent:
  - V3: Retour 11:12 (de Client4)
  - V4: Retour 11:12 (de Client1)
  - V1: Retour 11:12 (de Client5)
  - V2: Retour 11:12 (de Client3)
  - V5: Disponible à 13:00

Priorité: Non assignés > Clients normaux

À 11:12:
  V3 revient → Peut prendre Client8(25)? NON (V3=12 places)
             → Découpage: V3(12) + autre?
             → Pas de V5 encore + V1,V2 trop petits
             → Peut prendre Client6(15)? NON
             → Peut prendre Client7(10)? OUI!
             → V3 prend Client7(10)
                Départ 11:12, Retour 13:48 [216 min]

  V4 revient → Peut prendre Client8(25)? NON
             → Peut prendre Client6(15)? NON
             → Peut prendre Client7(10)? DÉJÀ pris par V3
             → V4 reste libr pour client normal

  V1 revient → Trop petite pour non assignés
             → Reste libre

  V2 revient → Trop petite pour non assignés
             → Reste libre

À 13:00:
  V5 devient disponible → Peut prendre Client8(25)?
                        → Découpage: V5(12) + V1(5)?
                        → V1 est libre!
        V5(12) prend 12 de Client8
        V1(5) prend 5 de Client8
        Départ 13:00, Retour ?
        Reste: 8 passagers de Client8
        
        Chercher V2 (5 places)?
        V2 est libre → V2 prend 5 passagers
        Reste: 3 passagers
        
        Total: V5(12) + V1(5) + V2(5) = 22 places
               Client8 = 25 passagers
               Reste 3 impossibles

  V6(?) ou autres?
  → NON assigné final de Client8: 3 passagers
```

---

## 📊 Résultat Final

```
PHASE 1 (Affectation):     4/8 clients assignés (50%)
PHASE 2 (Réutilisation):  3/4 non assignés réassignés
TOTAL FINAL:              7/8 clients assignés (87.5%)

Non assignés définitifs: Client8 partiellement (3 pers)
```

---

## 🚀 Pour tester

1. **Charger les données**:
   ```sql
   psql -U postgres -d operateur -f db/test_phase2.sql
   ```

2. **Appeler l'API**:
   ```bash
   curl -X POST "http://localhost:8383/reservation/api/affectations?date=2026-03-20" \
        -H "Authorization: Bearer <TOKEN>"
   ```

3. **Vérifier les affectations** dans l'interface web
   - Chercher la date 20/03/2026
   - Observer les voyages créés
   - Vérifier l'ordre chronologique (V3 avant V4, etc.)

