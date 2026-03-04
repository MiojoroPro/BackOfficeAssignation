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
     * Génère la clé de regroupement "vol" à partir d'un timestamp
     * Même vol = même heure:minute de départ
     */
    private String getVolKey(Timestamp dateHeureDepart) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateHeureDepart.getTime());
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    /**
     * Effectue l'affectation des véhicules pour toutes les réservations d'une date.
     * 
     * Algorithme:
     * 1. Regrouper par vol (même heure:minute)
     * 2. Pour chaque vol, trier par passagers DESC
     * 3. Prendre la plus grosse réservation, chercher un véhicule
     * 4. Essayer de combiner avec les réservations restantes du même vol
     * 5. Calculer la route de livraison (plus proche d'abord)
     * 6. Calculer le temps total et l'heure de retour
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
        
        // 1. Regrouper par vol (même heure:minute de départ)
        // TreeMap pour ordre chronologique des vols
        Map<String, List<Reservation>> groupesVol = new TreeMap<>();
        for (Reservation r : reservations) {
            String key = getVolKey(r.getDateHeureDepart());
            groupesVol.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }
        
        // Map pour suivre les créneaux occupés par véhicule
        Map<Integer, List<long[]>> vehiculesOccupes = new HashMap<>();
        
        // 2. Traiter chaque vol dans l'ordre chronologique
        for (Map.Entry<String, List<Reservation>> entry : groupesVol.entrySet()) {
            List<Reservation> groupe = entry.getValue();
            
            // Trier par nombre de passagers DECROISSANT
            groupe.sort((a, b) -> Integer.compare(b.getNombrePassagers(), a.getNombrePassagers()));
            
            // Liste des réservations en attente d'affectation
            List<Reservation> pending = new ArrayList<>(groupe);
            
            while (!pending.isEmpty()) {
                Reservation first = pending.remove(0);
                
                // Récupérer les véhicules candidats (capacité >= passagers de la première réservation)
                // Triés par capacité ASC, carburant ASC (D avant E)
                List<Vehicule> candidates = shuffleTies(vehiculeDao.findVehiculesCapables(first.getNombrePassagers()));
                
                boolean assigned = false;
                
                // 3. Essayer chaque véhicule candidat
                for (Vehicule vehicule : candidates) {
                    // Essayer de combiner des réservations du même vol
                    List<Reservation> combined = new ArrayList<>();
                    combined.add(first);
                    int remainingCapacity = vehicule.getCapacite() - first.getNombrePassagers();
                    
                    // Parcourir les réservations restantes (déjà triées par passagers DESC)
                    for (Reservation r : pending) {
                        if (r.getNombrePassagers() <= remainingCapacity) {
                            combined.add(r);
                            remainingCapacity -= r.getNombrePassagers();
                        }
                    }
                    
                    // 4. Calculer la route de livraison (plus proche d'abord)
                    List<RouteStop> route = calculerRoute(aeroport.getId(), combined);
                    
                    // 5. Calculer le temps total du trajet
                    int totalMinutes = calculerTempsRoute(aeroport.getId(), route, parametre);
                    
                    Timestamp departure = first.getDateHeureDepart();
                    Timestamp returnTime = new Timestamp(departure.getTime() + totalMinutes * 60 * 1000L);
                    
                    // 6. Vérifier la disponibilité du véhicule pour tout le créneau
                    if (estDisponible(vehicule.getId(), departure.getTime(), returnTime.getTime(), vehiculesOccupes)) {
                        // Affecter toutes les réservations combinées à ce véhicule
                        for (Reservation r : combined) {
                            int order = getStopOrder(r.getIdLieuDestination(), route);
                            affectationDao.insert(vehicule.getId(), r.getId(), departure, returnTime, order);
                            
                            // Construire l'objet de détails pour le résultat
                            AffectationDetails ad = new AffectationDetails();
                            ad.setIdVehicule(vehicule.getId());
                            ad.setImmatriculation(vehicule.getImmatriculation());
                            ad.setCapacite(vehicule.getCapacite());
                            ad.setCarburant(vehicule.getCarburant());
                            ad.setIdReservation(r.getId());
                            ad.setIdClient(r.getIdClient());
                            ad.setNombrePassagers(r.getNombrePassagers());
                            ad.setLieuDepart(r.getLieuDepart());
                            ad.setLieuArrivee(r.getLieuDestination());
                            ad.setDateHeureDepart(departure);
                            ad.setDateHeureRetour(returnTime);
                            ad.setOrdreLivraison(order);
                            resultat.addAffectation(ad);
                        }
                        
                        // Retirer les réservations combinées (sauf first déjà retiré) de pending
                        for (int i = 1; i < combined.size(); i++) {
                            pending.remove(combined.get(i));
                        }
                        
                        // Marquer le véhicule comme occupé
                        vehiculesOccupes.computeIfAbsent(vehicule.getId(), k -> new ArrayList<>())
                            .add(new long[]{departure.getTime(), returnTime.getTime()});
                        
                        assigned = true;
                        break;
                    }
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
     * Itinéraire: Aéroport → Arrêt1 (+ attente) → Arrêt2 (+ attente) → ... → Aéroport
     * 
     * @param aeroportId ID de l'aéroport
     * @param route Liste ordonnée des arrêts
     * @param parametre Paramètres (vitesse moyenne, temps d'attente)
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
            totalMinutes += parametre.getTempsAttente(); // Temps de dépose à chaque arrêt
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
    private List<Vehicule> shuffleTies(List<Vehicule> vehicles) {
        List<Vehicule> result = new ArrayList<>();
        int i = 0;
        while (i < vehicles.size()) {
            int j = i;
            while (j < vehicles.size()
                    && vehicles.get(j).getCapacite() == vehicles.get(i).getCapacite()
                    && vehicles.get(j).getCarburant() == vehicles.get(i).getCarburant()) {
                j++;
            }
            // Mélanger le sous-groupe [i, j) qui a même capacité et même carburant
            List<Vehicule> group = new ArrayList<>(vehicles.subList(i, j));
            Collections.shuffle(group, random);
            result.addAll(group);
            i = j;
        }
        return result;
    }

    /**
     * Vérifie si un véhicule est disponible pendant un créneau
     */
    private boolean estDisponible(int vehiculeId, long debut, long fin, Map<Integer, List<long[]>> vehiculesOccupes) {
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
}
