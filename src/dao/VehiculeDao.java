package dao;

import config.DbConfig;
import model.Vehicule;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehiculeDao {

    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT id, immatriculation, capacite, carburant FROM vehicule ORDER BY capacite, carburant";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                vehicules.add(mapVehicule(rs));
            }
        }
        return vehicules;
    }

    public Vehicule findById(int id) throws SQLException {
        String sql = "SELECT id, immatriculation, capacite, carburant FROM vehicule WHERE id = ?";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVehicule(rs);
                }
            }
            return null;
        }
    }

    /**
     * Trouve les véhicules avec capacité >= nombrePassagers
     * Triés par: capacité croissante, puis Diesel d'abord
     */
    public List<Vehicule> findVehiculesCapables(int nombrePassagers) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT id, immatriculation, capacite, carburant " +
                     "FROM vehicule " +
                     "WHERE capacite >= ? " +
                     "ORDER BY capacite ASC, carburant ASC"; // D avant E (alphabétique)
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, nombrePassagers);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Trouve les véhicules disponibles pour une date/heure donnée
     * Un véhicule est disponible s'il n'a pas d'affectation qui chevauche le créneau demandé
     */
    public List<Vehicule> findVehiculesDisponibles(int nombrePassagers, Timestamp dateHeureDepart, Timestamp dateHeureRetour) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.id, v.immatriculation, v.capacite, v.carburant " +
                     "FROM vehicule v " +
                     "WHERE v.capacite >= ? " +
                     "AND v.id NOT IN (" +
                     "    SELECT a.id_vehicule FROM affectation a " +
                     "    WHERE (a.date_heure_depart <= ? AND a.date_heure_retour >= ?)" +
                     ") " +
                     "ORDER BY v.capacite ASC, v.carburant ASC";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, nombrePassagers);
            ps.setTimestamp(2, dateHeureRetour);
            ps.setTimestamp(3, dateHeureDepart);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Retourne, pour chaque véhicule, son heure minimale de disponibilité pour la date donnée.
     * Si la colonne heure_disponibilite n'existe pas, tous les véhicules sont disponibles à 00:00.
     */
    public Map<Integer, Long> findHeuresDisponibiliteInitiale(Date date) throws SQLException {
        Map<Integer, Long> disponibilites = new HashMap<>();
        long debutJour = date.toLocalDate().atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

        try (Connection conn = DbConfig.getConnection()) {
            boolean hasHeureDisponibilite = hasHeureDisponibiliteColumn(conn);

            String sql = hasHeureDisponibilite
                ? "SELECT id, heure_disponibilite FROM vehicule"
                : "SELECT id FROM vehicule";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idVehicule = rs.getInt("id");
                    long dispoMs = debutJour;

                    if (hasHeureDisponibilite) {
                        Time heureDispo = rs.getTime("heure_disponibilite");
                        if (heureDispo != null) {
                            LocalTime lt = heureDispo.toLocalTime();
                            LocalDateTime ldt = LocalDateTime.of(date.toLocalDate(), lt);
                            dispoMs = Timestamp.valueOf(ldt).getTime();
                        }
                    }

                    disponibilites.put(idVehicule, dispoMs);
                }
            }
        }

        return disponibilites;
    }

    private boolean hasHeureDisponibiliteColumn(Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns " +
                     "WHERE table_schema = current_schema() " +
                     "AND table_name = 'vehicule' " +
                     "AND column_name = 'heure_disponibilite'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private Vehicule mapVehicule(ResultSet rs) throws SQLException {
        return new Vehicule(
            rs.getInt("id"),
            rs.getString("immatriculation"),
            rs.getInt("capacite"),
            rs.getString("carburant").charAt(0)
        );
    }
}
