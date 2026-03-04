package service;

import dao.*;
import model.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Service de gestion des affectations de véhicules aux réservations
 * Implémente les règles de gestion:
 * 1. Capacité >= nombre de passagers
 * 2. Capacité la plus proche du nombre de passagers
 * 3. Priorité au Diesel
 * 4. Si égalité parfaite → choix aléatoire
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
     * Effectue l'affectation des véhicules pour toutes les réservations d'une date
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
        
        // Trier par heure de départ pour traiter dans l'ordre chronologique
        reservations.sort(Comparator.comparing(Reservation::getDateHeureDepart));
        
        // Map pour suivre les créneaux occupés par véhicule
        Map<Integer, List<long[]>> vehiculesOccupes = new HashMap<>();
        
        for (Reservation reservation : reservations) {
            // Calculer le temps de trajet aller-retour
            double distanceKm = distanceDao.getDistanceAllerRetour(aeroport.getId(), reservation.getIdLieuDestination());
            int tempsTrajetMinutes = parametre.calculerTempsTrajet(distanceKm);
            
            // Calculer l'heure de retour (sans temps d'attente pour l'instant)
            Timestamp dateHeureDepart = reservation.getDateHeureDepart();
            Timestamp dateHeureRetour = new Timestamp(dateHeureDepart.getTime() + (tempsTrajetMinutes * 60 * 1000L));
            
            // Chercher un véhicule disponible
            Vehicule vehiculeChoisi = choisirVehicule(
                reservation.getNombrePassagers(), 
                dateHeureDepart, 
                dateHeureRetour,
                vehiculesOccupes
            );
            
            if (vehiculeChoisi != null) {
                // Créer l'affectation
                affectationDao.insert(vehiculeChoisi.getId(), reservation.getId(), dateHeureDepart, dateHeureRetour);
                
                // Marquer le véhicule comme occupé pendant ce créneau
                vehiculesOccupes.computeIfAbsent(vehiculeChoisi.getId(), k -> new ArrayList<>())
                    .add(new long[]{dateHeureDepart.getTime(), dateHeureRetour.getTime()});
                
                // Ajouter aux résultats
                AffectationDetails ad = new AffectationDetails();
                ad.setIdVehicule(vehiculeChoisi.getId());
                ad.setImmatriculation(vehiculeChoisi.getImmatriculation());
                ad.setCapacite(vehiculeChoisi.getCapacite());
                ad.setCarburant(vehiculeChoisi.getCarburant());
                ad.setIdReservation(reservation.getId());
                ad.setIdClient(reservation.getIdClient());
                ad.setNombrePassagers(reservation.getNombrePassagers());
                ad.setLieuDepart(reservation.getLieuDepart());
                ad.setLieuArrivee(reservation.getLieuDestination());
                ad.setDateHeureDepart(dateHeureDepart);
                ad.setDateHeureRetour(dateHeureRetour);
                resultat.addAffectation(ad);
            } else {
                resultat.addReservationNonAffectee(reservation);
            }
        }
        
        return resultat;
    }

    /**
     * Choisit le meilleur véhicule selon les règles de gestion
     */
    private Vehicule choisirVehicule(int nombrePassagers, Timestamp debut, Timestamp fin, 
                                     Map<Integer, List<long[]>> vehiculesOccupes) throws SQLException {
        // Récupérer tous les véhicules avec capacité suffisante
        List<Vehicule> vehiculesCapables = vehiculeDao.findVehiculesCapables(nombrePassagers);
        
        if (vehiculesCapables.isEmpty()) {
            return null;
        }
        
        // Filtrer les véhicules disponibles (non occupés pendant le créneau)
        List<Vehicule> vehiculesDisponibles = new ArrayList<>();
        for (Vehicule v : vehiculesCapables) {
            if (estDisponible(v.getId(), debut.getTime(), fin.getTime(), vehiculesOccupes)) {
                vehiculesDisponibles.add(v);
            }
        }
        
        if (vehiculesDisponibles.isEmpty()) {
            return null;
        }
        
        // Appliquer les règles de priorité
        // 1. Trouver la capacité minimale parmi les véhicules disponibles
        int capaciteMin = vehiculesDisponibles.stream()
            .mapToInt(Vehicule::getCapacite)
            .min()
            .orElse(Integer.MAX_VALUE);
        
        // 2. Filtrer les véhicules avec cette capacité
        List<Vehicule> vehiculesOptimaux = new ArrayList<>();
        for (Vehicule v : vehiculesDisponibles) {
            if (v.getCapacite() == capaciteMin) {
                vehiculesOptimaux.add(v);
            }
        }
        
        // 3. Prioriser les Diesel
        List<Vehicule> vehiculesDiesel = new ArrayList<>();
        for (Vehicule v : vehiculesOptimaux) {
            if (v.isDiesel()) {
                vehiculesDiesel.add(v);
            }
        }
        
        // 4. Si des véhicules Diesel disponibles, choisir parmi eux
        List<Vehicule> candidats = !vehiculesDiesel.isEmpty() ? vehiculesDiesel : vehiculesOptimaux;
        
        // 5. Si plusieurs candidats, choix aléatoire
        if (candidats.size() == 1) {
            return candidats.get(0);
        } else {
            return candidats.get(random.nextInt(candidats.size()));
        }
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
