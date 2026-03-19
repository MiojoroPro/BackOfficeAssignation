package service;

import dao.*;
import model.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Service de gestion des affectations de véhicules aux réservations
 * 
 * Nouvelle logique d'affectation:
 * 1. Regrouper les réservations par "vol" (même aéroport, même date, même heure:minute)
 * 2. Trier par nombre de passagers décroissant dans chaque groupe
 * 3. Chercher la voiture pour la plus grosse réservation, puis essayer de combiner les autres
 * 4. Livraison: lieu le plus proche d'abord, puis le plus proche du lieu courant, etc.
 * 5. En cas d'égalité de distance: ordre alphabétique du lieu
 * 
 * Règles de sélection de véhicule:
 * - Capacité >= nombre de passagers de la première réservation
 * - Capacité la plus proche
 * - Priorité au véhicule ayant le moins de trajets pour la date en cours
 * - Priorité au Diesel
 * - Si égalité parfaite → choix aléatoire
 */
public class AffectationService {

    private final ParametreDao parametreDao = new ParametreDao();
    private final LieuDao lieuDao = new LieuDao();
    private final DistanceDao distanceDao = new DistanceDao();
    private final VehiculeDao vehiculeDao = new VehiculeDao();
    private final ReservationDao reservationDao = new ReservationDao();
    private final AffectationDao affectationDao = new AffectationDao();
    
    private final Random random = new Random();

    /**
     * Résultat d'une tentative d'affectation
     */
    public static class ResultatAffectation {
        private List<AffectationDetails> affectationsReussies = new ArrayList<>();
        private List<Reservation> reservationsNonAffectees = new ArrayList<>();
        
        public List<AffectationDetails> getAffectationsReussies() {
            return affectationsReussies;
        }
        
        public List<Reservation> getReservationsNonAffectees() {
            return reservationsNonAffectees;
        }
        
        public void addAffectation(AffectationDetails ad) {
            affectationsReussies.add(ad);
        }
        
        public void addReservationNonAffectee(Reservation r) {
            reservationsNonAffectees.add(r);
        }
    }

    /**
     * Vue métier d'un voyage d'un véhicule (un créneau départ/retour).
     * Plusieurs réservations peuvent appartenir au même voyage.
     */
    public static class VoyageAffectation {
        private int idVehicule;
        private String immatriculation;
        private int capacite;
        private char carburant;
        private Timestamp dateHeureDepart;
        private Timestamp dateHeureRetour;
        private int totalPassagers;
        private final List<AffectationDetails> reservations = new ArrayList<>();

        public int getIdVehicule() {
            return idVehicule;
        }

        public void setIdVehicule(int idVehicule) {
            this.idVehicule = idVehicule;
        }

        public String getImmatriculation() {
            return immatriculation;
        }

        public void setImmatriculation(String immatriculation) {
            this.immatriculation = immatriculation;
        }

        public int getCapacite() {
            return capacite;
        }

        public void setCapacite(int capacite) {
            this.capacite = capacite;
        }

        public char getCarburant() {
            return carburant;
        }

        public void setCarburant(char carburant) {
            this.carburant = carburant;
        }

        public Timestamp getDateHeureDepart() {
            return dateHeureDepart;
        }

        public void setDateHeureDepart(Timestamp dateHeureDepart) {
            this.dateHeureDepart = dateHeureDepart;
        }

        public Timestamp getDateHeureRetour() {
            return dateHeureRetour;
        }

        public void setDateHeureRetour(Timestamp dateHeureRetour) {
            this.dateHeureRetour = dateHeureRetour;
        }

        public int getTotalPassagers() {
            return totalPassagers;
        }

        public void addPassagers(int passagers) {
            this.totalPassagers += passagers;
        }

        public List<AffectationDetails> getReservations() {
            return reservations;
        }

        public int getNombreReservations() {
            return reservations.size();
        }

        public String getCarburantLibelle() {
            return carburant == 'D' ? "Diesel" : "Essence";
        }

        public boolean isDiesel() {
            return carburant == 'D';
        }

        public String getTrajetLibelle() {
            if (reservations.isEmpty()) {
                return "";
            }

            Map<Integer, String> arrets = new TreeMap<>();
            for (AffectationDetails ad : reservations) {
                arrets.putIfAbsent(ad.getOrdreLivraison(), ad.getLieuArrivee());
            }

            StringBuilder sb = new StringBuilder(reservations.get(0).getLieuDepart());
            for (String destination : arrets.values()) {
                sb.append(" -> ").append(destination);
            }
            return sb.toString();
        }
    }

    /**
     * Représente un arrêt dans la route de livraison
     */
    private static class RouteStop {
        int lieuId;
        String lieuLibelle;
        int order; // 1-based

        RouteStop(int lieuId, String lieuLibelle, int order) {
            this.lieuId = lieuId;
            this.lieuLibelle = lieuLibelle;
            this.order = order;
        }
    }

    /**
     * Portion d'une réservation affectée à un véhicule (cas de découpage).
     */
    private static class PortionDecoupage {
        Vehicule vehicule;
        int passagersAffectes;

        PortionDecoupage(Vehicule vehicule, int passagersAffectes) {
            this.vehicule = vehicule;
            this.passagersAffectes = passagersAffectes;
        }
    }

    /**
     * Plan d'affectation d'une réservation découpée.
     */
    private static class PlanDecoupage {
        Timestamp departure;
        Timestamp returnTime;
        List<RouteStop> route;
        List<PortionDecoupage> portions;

        PlanDecoupage(Timestamp departure, Timestamp returnTime, List<RouteStop> route, List<PortionDecoupage> portions) {
            this.departure = departure;
            this.returnTime = returnTime;
            this.route = route;
            this.portions = portions;
        }
    }

    /**
     * Portion d'une réservation embarquée dans un véhicule pendant un voyage.
     */
    private static class ReservationPortion {
        Reservation reservation;
        int passagersAffectes;

        ReservationPortion(Reservation reservation, int passagersAffectes) {
            this.reservation = reservation;
            this.passagersAffectes = passagersAffectes;
        }
    }

    /**
     * Génère la clé de regroupement "vol" à partir d'un timestamp
     * Même vol = même heure:minute de départ
     * @deprecated Remplacé par le regroupement par fenêtre de temps d'attente
     */
    private String getVolKey(Timestamp dateHeureDepart) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateHeureDepart.getTime());
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }
    
    /**
     * Classe interne pour stocker un groupe de réservations avec son heure de départ effective
     */
    private static class GroupeReservations {
        List<Reservation> reservations;
        Timestamp heureDepartEffective;
        
        GroupeReservations(List<Reservation> reservations, Timestamp heureDepartEffective) {
            this.reservations = reservations;
            this.heureDepartEffective = heureDepartEffective;
        }
    }
    
    /**
     * Regroupe les réservations par fenêtre de temps d'attente.
     * 
     * Algorithme:
     * 1. Trier les réservations par heure de départ croissant
     * 2. Prendre la première réservation non groupée
     * 3. Créer une fenêtre [heure_depart, heure_depart + temps_attente]
     * 4. Ajouter toutes les réservations dans cette fenêtre au groupe
     * 5. L'heure de départ de base = heure de la dernière réservation du groupe
     * 6. Répéter pour les réservations restantes
     */
    private List<GroupeReservations> regrouperParFenetreTempAttente(List<Reservation> reservations, int tempsAttenteMinutes) {
        // Trier par heure de départ croissant
        reservations.sort(Comparator.comparing(Reservation::getDateHeureDepart));
        
        List<GroupeReservations> groupes = new ArrayList<>();
        Set<Integer> assigned = new HashSet<>();
        
        for (int i = 0; i < reservations.size(); i++) {
            if (assigned.contains(i)) continue;
            
            Reservation first = reservations.get(i);
            long windowStart = first.getDateHeureDepart().getTime();
            long windowEnd = windowStart + tempsAttenteMinutes * 60 * 1000L;
            
            List<Reservation> groupe = new ArrayList<>();
            groupe.add(first);
            assigned.add(i);
            long derniereHeureGroupe = first.getDateHeureDepart().getTime();
            
            // Ajouter les réservations dans la fenêtre de temps
            for (int j = i + 1; j < reservations.size(); j++) {
                if (assigned.contains(j)) continue;
                
                Reservation r = reservations.get(j);
                long departTime = r.getDateHeureDepart().getTime();
                if (departTime <= windowEnd) {
                    groupe.add(r);
                    assigned.add(j);
                    if (departTime > derniereHeureGroupe) {
                        derniereHeureGroupe = departTime;
                    }
                }
            }
            
            // Heure de départ de base = heure de la dernière réservation du groupe
            Timestamp heureDepartEffective = new Timestamp(derniereHeureGroupe);
            groupes.add(new GroupeReservations(groupe, heureDepartEffective));
        }
        
        return groupes;
    }

    /**
     * Effectue l'affectation des véhicules pour toutes les réservations d'une date.
     * 
     * Algorithme:
     * 1. Regrouper par fenêtre de temps d'attente (ex: 8h00-8h30 si temps_attente=30min)
     * 2. Pour chaque groupe, trier par passagers DESC
     * 3. Prendre la plus grosse réservation, chercher un véhicule
     * 4. Essayer de combiner avec les réservations restantes du groupe
     * 5. Calculer la route de livraison (plus proche d'abord)
     * 6. Calculer le temps total et l'heure de retour
    * 7. L'heure de départ effective = max(dernière réservation du groupe, disponibilité véhicule)
     */
    public ResultatAffectation affecterVehicules(Date date) throws SQLException {
        ResultatAffectation resultat = new ResultatAffectation();
        
        // Récupérer les paramètres
        Parametre parametre = parametreDao.getParametre();
        if (parametre == null) {
            throw new SQLException("Paramètres non configurés");
        }
        
        // Récupérer l'aéroport
        Lieu aeroport = lieuDao.findAeroport();
        if (aeroport == null) {
            throw new SQLException("Aéroport non configuré");
        }
        
        // Supprimer les anciennes affectations pour cette date
        affectationDao.deleteByDate(date);
        
        // Récupérer les réservations du jour
        List<Reservation> reservations = reservationDao.findByDate(date);
        
        // 1. Regrouper par fenêtre de temps d'attente
        List<GroupeReservations> groupes = regrouperParFenetreTempAttente(reservations, parametre.getTempsAttente());

        // Référentiel des véhicules (utile pour l'heure de disponibilité journalière)
        Map<Integer, Vehicule> vehiculesParId = new HashMap<>();
        for (Vehicule vehicule : vehiculeDao.findAll()) {
            vehiculesParId.put(vehicule.getId(), vehicule);
        }
        
        // Map pour suivre les créneaux occupés par véhicule
        Map<Integer, List<long[]>> vehiculesOccupes = new HashMap<>();
        // Map pour suivre le nombre de trajets par véhicule pour la date en cours
        Map<Integer, Integer> trajetsParVehicule = new HashMap<>();
        
        // Réservations reportées au groupe suivant (ex: découpage impossible au même départ)
        List<Reservation> reservationsReportees = new ArrayList<>();

        // 2. Traiter chaque groupe dans l'ordre chronologique
        for (int indexGroupe = 0; indexGroupe < groupes.size(); indexGroupe++) {
            GroupeReservations groupeRes = groupes.get(indexGroupe);
            List<Reservation> groupe = new ArrayList<>();
            if (!reservationsReportees.isEmpty()) {
                groupe.addAll(reservationsReportees);
                reservationsReportees.clear();
            }
            groupe.addAll(groupeRes.reservations);
            Timestamp heureDepartEffective = groupeRes.heureDepartEffective;
            
            // Trier par nombre de passagers DECROISSANT
            groupe.sort((a, b) -> Integer.compare(b.getNombrePassagers(), a.getNombrePassagers()));
            
            // Liste des réservations en attente d'affectation
            List<Reservation> pending = new ArrayList<>(groupe);

            // Traitement réservation par réservation (avec découpage si nécessaire)
            while (!pending.isEmpty()) {
                // Toujours reprendre la plus grosse demande restante
                pending.sort((a, b) -> Integer.compare(b.getNombrePassagers(), a.getNombrePassagers()));
                Reservation first = pending.remove(0);

                // Aucun véhicule ne peut prendre la réservation en un seul trajet: tenter le découpage.
                List<Vehicule> rawCandidates = vehiculeDao.findVehiculesCapables(first.getNombrePassagers());
                if (rawCandidates.isEmpty()) {
                    PlanDecoupage plan = trouverPlanDecoupageReservation(
                        first,
                        indexGroupe,
                        groupes,
                        heureDepartEffective,
                        aeroport.getId(),
                        parametre,
                        vehiculesOccupes,
                        trajetsParVehicule,
                        vehiculesParId
                    );

                    if (plan != null) {
                        int order = getStopOrder(first.getIdLieuDestination(), plan.route);
                        for (PortionDecoupage portion : plan.portions) {
                            enregistrerAffectation(
                                portion.vehicule,
                                first,
                                plan.departure,
                                plan.returnTime,
                                order,
                                portion.passagersAffectes,
                                resultat
                            );

                            vehiculesOccupes.computeIfAbsent(portion.vehicule.getId(), k -> new ArrayList<>())
                                .add(new long[]{plan.departure.getTime(), plan.returnTime.getTime()});
                            incrementerTrajets(portion.vehicule.getId(), trajetsParVehicule);
                        }
                    } else if (indexGroupe < groupes.size() - 1) {
                        // Pas de départ commun possible maintenant: on reporte au groupe suivant.
                        reservationsReportees.add(first);
                    } else {
                        resultat.addReservationNonAffectee(first);
                    }
                    continue;
                }

                // Récupérer les véhicules candidats (capacité >= passagers de la première réservation)
                // Triés par capacité la plus proche de la réservation, puis règles de priorité
                List<Vehicule> candidates = ordonnerVehiculesParProximite(
                    rawCandidates,
                    first.getNombrePassagers(),
                    trajetsParVehicule
                );
                
                boolean assigned = false;
                
                // 3. Essayer chaque véhicule candidat
                for (Vehicule vehicule : candidates) {
                    // Côté véhicule: remplir avec les portions de réservations les plus proches du reste de places.
                    List<ReservationPortion> portions = composerPortionsPourVehicule(first, pending, vehicule.getCapacite());
                    List<Reservation> reservationsPourRoute = extraireReservationsDistinctes(portions);
                    
                    // 4. Calculer la route de livraison (plus proche d'abord)
                    List<RouteStop> route = calculerRoute(aeroport.getId(), reservationsPourRoute);
                    
                    // 5. Calculer le temps total du trajet
                    int totalMinutes = calculerTempsRoute(aeroport.getId(), route, parametre);

                    long dureeMs = totalMinutes * 60 * 1000L;
                    long departMs = trouverPremierDepartDisponible(
                        vehicule.getId(),
                        heureDepartEffective.getTime(),
                        dureeMs,
                        vehiculesOccupes,
                        vehiculesParId
                    );

                    Timestamp departure = new Timestamp(departMs);
                    Timestamp returnTime = new Timestamp(departMs + dureeMs);

                    // Affecter les portions retenues dans ce véhicule
                    for (ReservationPortion portion : portions) {
                        int order = getStopOrder(portion.reservation.getIdLieuDestination(), route);
                        enregistrerAffectation(
                            vehicule,
                            portion.reservation,
                            departure,
                            returnTime,
                            order,
                            portion.passagersAffectes,
                            resultat
                        );
                    }

                    // Marquer le véhicule comme occupé
                    vehiculesOccupes.computeIfAbsent(vehicule.getId(), k -> new ArrayList<>())
                        .add(new long[]{departure.getTime(), returnTime.getTime()});
                    incrementerTrajets(vehicule.getId(), trajetsParVehicule);

                    assigned = true;
                    break;
                }
                
                if (!assigned) {
                    resultat.addReservationNonAffectee(first);
                }
            }
        }
        
        return resultat;
    }

    /**
     * Calcule la route de livraison optimale avec l'algorithme du plus proche voisin.
     * 
     * Depuis l'aéroport, on va vers la destination la plus proche,
     * puis depuis cette destination vers la plus proche restante, etc.
     * En cas d'égalité de distance, on trie par ordre alphabétique du nom du lieu.
     * 
     * @param aeroportId ID de l'aéroport (point de départ)
     * @param reservations Liste des réservations à livrer
     * @return Liste ordonnée des arrêts avec leur ordre de livraison
     */
    private List<RouteStop> calculerRoute(int aeroportId, List<Reservation> reservations) throws SQLException {
        // Extraire les destinations distinctes
        Map<Integer, String> destinations = new LinkedHashMap<>();
        for (Reservation r : reservations) {
            destinations.putIfAbsent(r.getIdLieuDestination(), r.getLieuDestination());
        }
        
        List<RouteStop> route = new ArrayList<>();
        Set<Integer> remaining = new HashSet<>(destinations.keySet());
        int currentId = aeroportId;
        int order = 1;
        
        while (!remaining.isEmpty()) {
            // Trouver la destination la plus proche du lieu courant
            int nearestId = -1;
            String nearestLibelle = null;
            double nearestDist = Double.MAX_VALUE;
            
            for (int destId : remaining) {
                double dist = distanceDao.getDistanceKm(currentId, destId);
                String libelle = destinations.get(destId);
                
                // Plus proche, ou même distance → ordre alphabétique
                if (dist < nearestDist || 
                    (dist == nearestDist && nearestLibelle != null && libelle.compareTo(nearestLibelle) < 0)) {
                    nearestDist = dist;
                    nearestId = destId;
                    nearestLibelle = libelle;
                }
            }
            
            route.add(new RouteStop(nearestId, nearestLibelle, order));
            remaining.remove(nearestId);
            currentId = nearestId;
            order++;
        }
        
        return route;
    }

    /**
     * Calcule le temps total du trajet en minutes.
     * 
     * Itinéraire: Aéroport → Arrêt1 → Arrêt2 → ... → Aéroport
     * Le temps d'attente paramètre est utilisé uniquement pour le regroupement des réservations,
     * pas comme temps d'arrêt de livraison.
     * 
     * @param aeroportId ID de l'aéroport
     * @param route Liste ordonnée des arrêts
     * @param parametre Paramètres (vitesse moyenne)
     * @return Temps total en minutes
     */
    private int calculerTempsRoute(int aeroportId, List<RouteStop> route, Parametre parametre) throws SQLException {
        if (route.isEmpty()) return 0;
        
        double totalMinutes = 0;
        int currentId = aeroportId;
        
        // Parcourir chaque arrêt
        for (RouteStop stop : route) {
            double distKm = distanceDao.getDistanceKm(currentId, stop.lieuId);
            totalMinutes += Math.ceil((distKm / parametre.getVitesseMoyenne()) * 60);
            currentId = stop.lieuId;
        }
        
        // Retour vers l'aéroport depuis le dernier arrêt
        double retourKm = distanceDao.getDistanceKm(currentId, aeroportId);
        totalMinutes += Math.ceil((retourKm / parametre.getVitesseMoyenne()) * 60);
        
        return (int) totalMinutes;
    }

    /**
     * Retourne l'ordre de livraison d'un lieu dans la route
     */
    private int getStopOrder(int lieuDestinationId, List<RouteStop> route) {
        for (RouteStop stop : route) {
            if (stop.lieuId == lieuDestinationId) {
                return stop.order;
            }
        }
        return 0;
    }

    /**
     * Mélange les véhicules de même priorité (même capacité + même carburant)
     * pour introduire l'aléatoire en cas d'égalité parfaite.
     * L'ordre global est préservé: capacité ASC, Diesel avant Essence.
     */
    private List<Vehicule> ordonnerVehiculesParPriorite(List<Vehicule> vehicles, Map<Integer, Integer> trajetsParVehicule) {
        vehicles.sort(
            Comparator
                .comparingInt(Vehicule::getCapacite)
                .thenComparingInt(v -> trajetsParVehicule.getOrDefault(v.getId(), 0))
                .thenComparing(Vehicule::getCarburant)
        );
        return melangerEgalites(vehicles, trajetsParVehicule);
    }

    /**
     * Mélange les véhicules de même priorité (même capacité, mêmes trajets, même carburant)
     * pour introduire l'aléatoire en cas d'égalité parfaite.
     */
    private List<Vehicule> melangerEgalites(List<Vehicule> vehicles, Map<Integer, Integer> trajetsParVehicule) {
        List<Vehicule> result = new ArrayList<>();
        int i = 0;
        while (i < vehicles.size()) {
            int j = i;
            int capacite = vehicles.get(i).getCapacite();
            int trajets = trajetsParVehicule.getOrDefault(vehicles.get(i).getId(), 0);
            char carburant = vehicles.get(i).getCarburant();

            while (j < vehicles.size()
                    && vehicles.get(j).getCapacite() == capacite
                    && trajetsParVehicule.getOrDefault(vehicles.get(j).getId(), 0) == trajets
                    && vehicles.get(j).getCarburant() == carburant) {
                j++;
            }

            List<Vehicule> group = new ArrayList<>(vehicles.subList(i, j));
            Collections.shuffle(group, random);
            result.addAll(group);
            i = j;
        }
        return result;
    }

    private void incrementerTrajets(int vehiculeId, Map<Integer, Integer> trajetsParVehicule) {
        trajetsParVehicule.put(vehiculeId, trajetsParVehicule.getOrDefault(vehiculeId, 0) + 1);
    }

    /**
     * Enregistre une affectation en base et dans le résultat métier.
     */
    private void enregistrerAffectation(
        Vehicule vehicule,
        Reservation reservation,
        Timestamp departure,
        Timestamp returnTime,
        int order,
        int passagersAffectes,
        ResultatAffectation resultat
    ) throws SQLException {
        affectationDao.insert(
            vehicule.getId(),
            reservation.getId(),
            departure,
            returnTime,
            order,
            passagersAffectes
        );

        AffectationDetails ad = new AffectationDetails();
        ad.setIdVehicule(vehicule.getId());
        ad.setImmatriculation(vehicule.getImmatriculation());
        ad.setCapacite(vehicule.getCapacite());
        ad.setCarburant(vehicule.getCarburant());
        ad.setIdReservation(reservation.getId());
        ad.setIdClient(reservation.getIdClient());
        ad.setNombrePassagers(passagersAffectes);
        ad.setLieuDepart(reservation.getLieuDepart());
        ad.setLieuArrivee(reservation.getLieuDestination());
        ad.setDateHeureDepart(departure);
        ad.setDateHeureRetour(returnTime);
        ad.setOrdreLivraison(order);
        resultat.addAffectation(ad);
    }

    /**
     * Cherche un plan de découpage d'une réservation en plusieurs véhicules.
     * Toutes les voitures du découpage doivent partir exactement au même moment.
     * Si impossible au groupe courant, on tentera au groupe suivant.
     */
    private PlanDecoupage trouverPlanDecoupageReservation(
        Reservation reservation,
        int indexGroupe,
        List<GroupeReservations> groupes,
        Timestamp departBase,
        int aeroportId,
        Parametre parametre,
        Map<Integer, List<long[]>> vehiculesOccupes,
        Map<Integer, Integer> trajetsParVehicule,
        Map<Integer, Vehicule> vehiculesParId
    ) throws SQLException {
        List<RouteStop> route = calculerRoute(aeroportId, Collections.singletonList(reservation));
        int totalMinutes = calculerTempsRoute(aeroportId, route, parametre);
        long dureeMs = totalMinutes * 60 * 1000L;

        // Départs testés: groupe courant puis vagues suivantes.
        LinkedHashSet<Long> departsCandidats = new LinkedHashSet<>();
        departsCandidats.add(departBase.getTime());
        for (int i = indexGroupe + 1; i < groupes.size(); i++) {
            departsCandidats.add(groupes.get(i).heureDepartEffective.getTime());
        }

        List<Vehicule> vehicules = ordonnerVehiculesParPriorite(
            vehiculeDao.findVehiculesCapables(1),
            trajetsParVehicule
        );

        for (long departMs : departsCandidats) {
            long finMs = departMs + dureeMs;

            List<Vehicule> disponiblesMemeDepart = new ArrayList<>();
            for (Vehicule vehicule : vehicules) {
                if (estDisponible(vehicule.getId(), departMs, finMs, vehiculesOccupes, vehiculesParId)) {
                    disponiblesMemeDepart.add(vehicule);
                }
            }

            if (disponiblesMemeDepart.isEmpty()) {
                continue;
            }

            int restants = reservation.getNombrePassagers();
            List<PortionDecoupage> portions = new ArrayList<>();

            List<Vehicule> pool = new ArrayList<>(disponiblesMemeDepart);
            while (restants > 0 && !pool.isEmpty()) {
                Vehicule vehicule = trouverVehiculePlusProche(pool, restants, trajetsParVehicule);
                int passagersAffectes = Math.min(restants, vehicule.getCapacite());
                portions.add(new PortionDecoupage(vehicule, passagersAffectes));
                restants -= passagersAffectes;
                pool.remove(vehicule);
            }

            if (restants == 0) {
                return new PlanDecoupage(
                    new Timestamp(departMs),
                    new Timestamp(finMs),
                    route,
                    portions
                );
            }
        }

        return null;
    }

    /**
     * Retourne la première heure de départ >= departSouhaite qui ne chevauche aucun créneau.
     */
    private long trouverPremierDepartDisponible(
        int vehiculeId,
        long departSouhaite,
        long dureeTrajetMs,
        Map<Integer, List<long[]>> vehiculesOccupes,
        Map<Integer, Vehicule> vehiculesParId
    ) {
        long departMinJournalier = calculerDebutDisponibiliteJournalier(vehiculeId, departSouhaite, vehiculesParId);
        long departInitial = Math.max(departSouhaite, departMinJournalier);

        List<long[]> creneaux = vehiculesOccupes.get(vehiculeId);
        if (creneaux == null || creneaux.isEmpty()) {
            return departInitial;
        }

        List<long[]> tries = new ArrayList<>(creneaux);
        tries.sort(Comparator.comparingLong(c -> c[0]));

        long depart = departInitial;
        boolean collision;
        do {
            collision = false;
            long fin = depart + dureeTrajetMs;
            for (long[] creneau : tries) {
                if (depart < creneau[1] && fin > creneau[0]) {
                    depart = creneau[1];
                    collision = true;
                    break;
                }
            }
        } while (collision);

        return depart;
    }

    private long calculerDebutDisponibiliteJournalier(int vehiculeId, long referenceMs, Map<Integer, Vehicule> vehiculesParId) {
        Vehicule vehicule = vehiculesParId.get(vehiculeId);
        if (vehicule == null) {
            return referenceMs;
        }

        java.sql.Time heureDisponibilite = vehicule.getHeureDisponibiliteEffective();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(referenceMs);

        Calendar h = Calendar.getInstance();
        h.setTimeInMillis(heureDisponibilite.getTime());

        cal.set(Calendar.HOUR_OF_DAY, h.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, h.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, h.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Vérifie si un véhicule est disponible pendant un créneau
     */
    private boolean estDisponible(
        int vehiculeId,
        long debut,
        long fin,
        Map<Integer, List<long[]>> vehiculesOccupes,
        Map<Integer, Vehicule> vehiculesParId
    ) {
        if (debut < calculerDebutDisponibiliteJournalier(vehiculeId, debut, vehiculesParId)) {
            return false;
        }

        List<long[]> creneaux = vehiculesOccupes.get(vehiculeId);
        if (creneaux == null || creneaux.isEmpty()) {
            return true;
        }
        
        for (long[] creneau : creneaux) {
            // Vérifier le chevauchement
            if (debut < creneau[1] && fin > creneau[0]) {
                return false;
            }
        }
        return true;
    }

    private List<ReservationPortion> composerPortionsPourVehicule(Reservation first, List<Reservation> pending, int capaciteVehicule) {
        List<ReservationPortion> portions = new ArrayList<>();
        portions.add(new ReservationPortion(first, first.getNombrePassagers()));

        int restant = capaciteVehicule - first.getNombrePassagers();
        while (restant > 0 && !pending.isEmpty()) {
            Reservation proche = trouverReservationPlusProche(pending, restant);
            if (proche == null) {
                break;
            }

            int passagersAffectes = Math.min(restant, proche.getNombrePassagers());
            portions.add(new ReservationPortion(proche, passagersAffectes));

            if (passagersAffectes == proche.getNombrePassagers()) {
                pending.remove(proche);
            } else {
                proche.setNombrePassagers(proche.getNombrePassagers() - passagersAffectes);
            }

            restant -= passagersAffectes;
        }

        return portions;
    }

    private Reservation trouverReservationPlusProche(List<Reservation> reservations, int cible) {
        Reservation meilleure = null;
        int meilleurEcart = Integer.MAX_VALUE;

        for (Reservation r : reservations) {
            int ecart = Math.abs(r.getNombrePassagers() - cible);
            if (ecart < meilleurEcart) {
                meilleure = r;
                meilleurEcart = ecart;
            } else if (ecart == meilleurEcart && meilleure != null && r.getNombrePassagers() > meilleure.getNombrePassagers()) {
                // A écart égal, prioriser la plus grande réservation pour compléter au mieux via découpage partiel.
                meilleure = r;
            }
        }

        return meilleure;
    }

    private List<Reservation> extraireReservationsDistinctes(List<ReservationPortion> portions) {
        LinkedHashMap<Integer, Reservation> map = new LinkedHashMap<>();
        for (ReservationPortion portion : portions) {
            map.putIfAbsent(portion.reservation.getId(), portion.reservation);
        }
        return new ArrayList<>(map.values());
    }

    private List<Vehicule> ordonnerVehiculesParProximite(
        List<Vehicule> vehicles,
        int ciblePassagers,
        Map<Integer, Integer> trajetsParVehicule
    ) {
        vehicles.sort(
            Comparator
                .comparingInt((Vehicule v) -> Math.abs(v.getCapacite() - ciblePassagers))
                .thenComparingInt(Vehicule::getCapacite)
                .thenComparingInt(v -> trajetsParVehicule.getOrDefault(v.getId(), 0))
                .thenComparing(Vehicule::getCarburant)
        );
        return melangerEgalitesParProximite(vehicles, ciblePassagers, trajetsParVehicule);
    }

    private List<Vehicule> melangerEgalitesParProximite(
        List<Vehicule> vehicles,
        int ciblePassagers,
        Map<Integer, Integer> trajetsParVehicule
    ) {
        List<Vehicule> result = new ArrayList<>();
        int i = 0;
        while (i < vehicles.size()) {
            int j = i;
            int ecart = Math.abs(vehicles.get(i).getCapacite() - ciblePassagers);
            int capacite = vehicles.get(i).getCapacite();
            int trajets = trajetsParVehicule.getOrDefault(vehicles.get(i).getId(), 0);
            char carburant = vehicles.get(i).getCarburant();

            while (j < vehicles.size()
                    && Math.abs(vehicles.get(j).getCapacite() - ciblePassagers) == ecart
                    && vehicles.get(j).getCapacite() == capacite
                    && trajetsParVehicule.getOrDefault(vehicles.get(j).getId(), 0) == trajets
                    && vehicles.get(j).getCarburant() == carburant) {
                j++;
            }

            List<Vehicule> group = new ArrayList<>(vehicles.subList(i, j));
            Collections.shuffle(group, random);
            result.addAll(group);
            i = j;
        }
        return result;
    }

    private Vehicule trouverVehiculePlusProche(List<Vehicule> vehicules, int cible, Map<Integer, Integer> trajetsParVehicule) {
        return vehicules.stream()
            .min(
                Comparator
                    .comparingInt((Vehicule v) -> Math.abs(v.getCapacite() - cible))
                    .thenComparingInt(Vehicule::getCapacite)
                    .thenComparingInt(v -> trajetsParVehicule.getOrDefault(v.getId(), 0))
                    .thenComparing(Vehicule::getCarburant)
            )
            .orElse(null);
    }

    /**
     * Récupère les affectations pour une date
     */
    public List<AffectationDetails> getAffectationsByDate(Date date) throws SQLException {
        return affectationDao.findDetailsByDate(date);
    }

    /**
     * Récupère les réservations non affectées pour une date
     */
    public List<Reservation> getReservationsNonAffectees(Date date) throws SQLException {
        return reservationDao.findNonAffecteesByDate(date);
    }

    /**
     * Groupe les affectations par véhicule
     */
    public Map<String, List<AffectationDetails>> groupByVehicule(List<AffectationDetails> affectations) {
        Map<String, List<AffectationDetails>> grouped = new LinkedHashMap<>();
        for (AffectationDetails ad : affectations) {
            String key = ad.getImmatriculation();
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(ad);
        }
        return grouped;
    }

    /**
     * Groupe les affectations par véhicule puis par voyage.
     * Un voyage correspond à un même créneau départ/retour pour un véhicule.
     */
    public Map<String, List<VoyageAffectation>> groupByVehiculeEtVoyage(List<AffectationDetails> affectations) {
        Map<String, Map<String, VoyageAffectation>> temp = new LinkedHashMap<>();

        for (AffectationDetails ad : affectations) {
            String vehiculeKey = ad.getImmatriculation();
            String voyageKey = ad.getDateHeureDepart().getTime() + "_" + ad.getDateHeureRetour().getTime();

            Map<String, VoyageAffectation> voyages = temp.computeIfAbsent(vehiculeKey, k -> new LinkedHashMap<>());

            VoyageAffectation voyage = voyages.computeIfAbsent(voyageKey, k -> {
                VoyageAffectation v = new VoyageAffectation();
                v.setIdVehicule(ad.getIdVehicule());
                v.setImmatriculation(ad.getImmatriculation());
                v.setCapacite(ad.getCapacite());
                v.setCarburant(ad.getCarburant());
                v.setDateHeureDepart(ad.getDateHeureDepart());
                v.setDateHeureRetour(ad.getDateHeureRetour());
                return v;
            });

            voyage.getReservations().add(ad);
            voyage.addPassagers(ad.getNombrePassagers());
        }

        Map<String, List<VoyageAffectation>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, VoyageAffectation>> entry : temp.entrySet()) {
            List<VoyageAffectation> voyages = new ArrayList<>(entry.getValue().values());
            voyages.sort(Comparator.comparing(VoyageAffectation::getDateHeureDepart));

            for (VoyageAffectation voyage : voyages) {
                voyage.getReservations().sort(
                    Comparator
                        .comparingInt(AffectationDetails::getOrdreLivraison)
                        .thenComparingInt(AffectationDetails::getIdReservation)
                );
            }

            grouped.put(entry.getKey(), voyages);
        }

        return grouped;
    }
}
