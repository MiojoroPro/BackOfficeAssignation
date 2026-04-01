# Règles de Gestion du Système d'Affectation de Véhicules

## 1. Regroupement par Fenêtre de Temps

### Règle
Les réservations sont groupées par **fenêtre de temps d'attente**, définie dans les paramètres (par défaut: 30 minutes).

### Exemple
- **Paramètre**: `temps_attente = 30 minutes`
- **Réservations**:
  - Client1: 08:00 → 7 passagers
  - Client2: 08:00 → 20 passagers
  - Client3: 09:10 → 3 passagers
  - Client4: 09:15 → 10 passagers
  - Client5: 09:20 → 5 passagers
  - Client6: 13:30 → 12 passagers

### Résultat du regroupement
- **Groupe 1** (08:00 - 08:30): Client1 + Client2
- **Groupe 2** (09:10 - 09:40): Client3 + Client4 + Client5
- **Groupe 3** (13:30 - 14:00): Client6

### Détails
- Les clients dans la même fenêtre **partent ensemble** à l'heure effective du groupe
- L'heure effective = `max(heure_dernière_réservation_groupe, disponibilité_véhicule)`

---

## 2. Affectation par Groupe et Combinaison

### Règle Principale
Pour chaque réservation du groupe:
1. Chercher un **seul véhicule** capable de prendre la réservation **complètement** (capacité ≥ nombres passagers)
2. Si trouvé → le réserver et passer à la réservation suivante
3. Si non trouvé → essayer de **combiner avec d'autres réservations** du même groupe dans ce véhicule

### Logique de Combinaison
Pour un véhicule candidat:
1. Ajouter la réservation actuelle
2. Parcourir les autres réservations du groupe (triées par passagers DESC)
3. Ajouter chaque réservation qui **rentre dans la capacité restante**

### Exemple
- **Véhicule3**: capacité = 12 places
- **Réservations à combiner**: Client1 (7 pax) + Client2 (20 pax)
- Client1 (7) rentre dans le véhicule → capacité restante = 5
- Client2 (20) ne rentre pas dans les 5 places restantes → non combiné

---

## 3. Sélection et Ordre de Priorité des Véhicules

### Critères de Tri (par ordre)
1. **Capacité ASC** (la plus petite d'abord, mais ≥ passagers demandés)
2. **Carburant** (Diesel 'D' avant Essence 'E')
3. **Heure de disponibilité ASC** (disponibles plus tôt en priorité)
4. **Nombre de trajets ASC** (fewer trips already assigned)
5. **Ordre aléatoire** (en cas d'égalité parfaite)

### Exemple
```
Véhicules capables de 20 passagers:
- Véhicule3: capacité=12 (ne peut pas) ❌
- Véhicule4: capacité=9 (ne peut pas) ❌
- Véhicule5: capacité=12, dispo=13:00, trajets=0 ✅
- (Pas d'autre véhicule apte)
```

---

## 4. Splitting / Découpage de Réservation

### Quand Splitter?
Lorsqu'**aucun véhicule** n'a une capacité ≥ nombre de passagers de la réservation.

### Règle de Splitting
1. Appeler `trouverPlanDecoupageReservation()`
2. Cette méthode cherche un ensemble de véhicules qui peuvent prendre la réservation **découpée**
3. Tous les véhicules du plan doivent **partir au même moment**
4. Si impossible au groupe courant → **reporter au groupe suivant**
5. Si reporté jusqu'au dernier groupe → **nonaffectée**

### Exemple
- **Réservation Client2**: 20 passagers
- **Aucun véhicule** avec capacité ≥ 20
- **Splitting nécessaire**:
  - Véhicule1 (capacité 5): 5 passagers
  - Véhicule2 (capacité 5): 5 passagers
  - Véhicule3 (capacité 12): 10 passagers
  - **Total**: 20 passagers répartis ✅

---

## 5. Disponibilité des Véhicules

### Concept
Chaque véhicule peut avoir une **heure minimale de disponibilité** stockée dans `vehicule.heure_disponibilite`.

### Exemple
- Véhicule1, 2, 3, 4: `heure_disponibilite = 00:00` (disponibles dès le départ)
- Véhicule5: `heure_disponibilite = 13:00` (disponible seulement à partir de 13h)

### Impact
- Un véhicule ne peut **pas départ avant son heure de disponibilité**
- S'il n'y a pas assez de véhicules disponibles pour une requ, la réservation est **reportée/non affectée**

---

## 6. Calcul de la Route de Livraison

### Algorithme: Nearest Neighbor (Plus Proche Voisin)

#### Étapes
1. Partir de l'**aéroport**
2. Trouver la destination **la plus proche** (en distance km)
3. Se rendre à cette destination
4. Depuis ce lieu, trouver la destination **la plus proche restante**
5. Répéter jusqu'à toutes les destinations visitées
6. **Retour à l'aéroport**

#### Exemple
- **Aéroport** → Hôtel2 (35 km) [plus proche]
- **Hôtel2** → Hôtel1 (60 km) [seule destination restante]
- **Hôtel1** → **Aéroport** (90 km) [retour]
- **Route totale**: 35 + 60 + 90 = 185 km

#### Règle de Déambiguïsation
- Si deux destinations sont à **même distance** → trier par **ordre alphabétique**

---

## 7. Calcul du Temps de Trajet

### Formule
```
Temps total (minutes) = Σ(distance_segment_km / parametre.vitesse_moyenne_kmh) × 60
```

### Paramètres
- **Vitesse moyenne**: définie dans la table `parametre` (ex: 50 km/h)
- **Arrêts de livraison**: le temps d'arrêt **n'est pas compté**

### Segments
- Aéroport → Destination1
- Destination1 → Destination2
- ...
- Dernière Destination → Aéroport

### Exemple
- Route: Aéroport → Hôtel2 (35 km) → Hôtel1 (60 km) → Aéroport (90 km)
- Vitesse: 50 km/h
- Temps: (35/50)×60 + (60/50)×60 + (90/50)×60 = 42 + 72 + 108 = **222 minutes** (3h42min)

---

## 8. Heure de Départ Effective

### Règle
```
Heure départ effective = MAX(
    heure_dernière_réservation_groupe,
    heure_disponibilité_véhicule
)
```

### Exemple
- **Groupe 2**: dernière réservation à 09:20
- **Véhicule4**: disponible à 00:00
- **Départ effectif**: 09:20

### Cas avec Disponibilité Retardée
- **Groupe 3**: réservation à 13:30
- **Véhicule5**: disponible à 13:00 (spécifié)
- **Départ effectif**: MAX(13:30, 13:00) = **13:30**

---

## 9. Suivi des Créneaux Occupés

### Concept
Pour éviter les **chevauchements de trajets**, chaque véhicule tracked ses:
- **Heure de départ** du trajet
- **Heure de retour** du trajet

### Règle
Un véhicule **ne peut pas être réservé** s'il y a **chevauchement** avec un créneau existant.

### Exemple
- Véhicule3: Trajet 1 = 08:00-09:24
- Tentative: affecter à 09:10-10:45 ❌ **CHEVAUCHEMENT** (09:10-09:24)
- Tentative: affecter à 09:30-11:00 ✅ **OK** (09:30 ≥ 09:24)

---

## 10. Résultats et Statut des Réservations

### Trois Catégories

#### A. Affectées avec Succès
- Réservations complètement affectées à un ou plusieurs véhicules
- Chaque affectation a: vehicule, passagers, départ, retour, numéro livraison

#### B. Reportées entre Groupes
- Utilisé si splitting impossible au groupe courant
- La réservation est reportée au **groupe suivant**

#### C. Non Affectées
- Aucune solution trouvée (même après report aux groupes suivants)
- Raisons possibles:
  - Pas assez de véhicules disponibles
  - Manque capacité totale même après splitting
  - Pas assez de créneaux libres pour tous les véhicules nécessaires

---

## 11. Affichage des Résultats

### Groupement par Véhicule et Voyage
- Les affectations sont groupées par:
  1. Véhicule (immatriculation)
  2. Voyage (créneau départ/retour)
- Triées **globalement par heure de départ** (tous véhicules confondus)

### Exemple d'Affichage
```
VÉHICULE3
  Départ: 08:00  Retour: 09:24
  - Client1 (7 passagers)

VÉHICULE1
  Départ: 09:20  Retour: 11:36
  - Client2 (5/20 passagers) [SPLIT]

VÉHICULE2
  Départ: 09:20  Retour: 11:36
  - Client2 (5/20 passagers) [SPLIT]

VÉHICULE5
  Départ: 13:30  Retour: 14:50
  - Client6 (12 passagers)
```

---

## 12. Ordonnancement des Livraisons

### Concept
Chaque arrêt (destination) dans une route a un **numéro d'ordre de livraison**.

### Exemple
```
Route: Aéroport → Hôtel2 → Hôtel1 → Aéroport
Livraisons:
- Hôtel2: ordre 1
- Hôtel1: ordre 2
```

### Usage
- Permet de savoir **l'ordre dans lequel** les clients sont livrés
- Important pour les clients qui veulent savoir à quelle heure approximative ils seront livrés

---

## Résumé du Flux Complet

1. **Récupérer** les réservations du jour
2. **Regrouper** par fenêtre de temps (ex: 30 min)
3. Pour chaque groupe:
   - Trier par passagers DESC
   - Pour chaque réservation non affectée:
     - Chercher véhicules capables
     - Si trouvé: essayer combinaison avec autres réservations
     - Si non trouvé: appeler splitting
4. **Enregistrer** tous les trajets en base de données
5. **Afficher** les résultats groupés par véhicule et voyage

---

## Notes Importantes

- ⚠️ **Granularité**: Le système travaille avec des **réservations**, pas des passagers individuels
- ⚠️ **Splitting**: Le découpage garde la réservation groupée (tous les passagers d'une réservation partent au même moment)
- ⚠️ **Fenêtres**: Les clients dans la même fenêtre partent **ensemble** (même heure de départ)
- ✅ **Capacité**: La somme des capacités affectées peut être > capacité du groupe (par splitting)

## SPRINT 8
PHASE 1 (existant): Affectation normale
├─ Groupes par fenêtre temps
├─ Tri CROISSANT (petits groupes OK)
└─ Certaines réservations → non assignées

PHASE 2 (NOUVEAU): Réutilisation véhicules
├─ Non assignés triés DÉCROISSANT (priorité gros)
├─ Pour chaque véhicule qui revient
├─ Assigner les non assignés immédiatement  
└─ Départ = heure retour du dernier trajet du véhicule
