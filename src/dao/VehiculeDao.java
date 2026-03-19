package dao;

import config.DbConfig;
import model.Vehicule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDao {

    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT id, immatriculation, capacite, carburant, heure_disponibilite FROM vehicule ORDER BY capacite, carburant";
        
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
        String sql = "SELECT id, immatriculation, capacite, carburant, heure_disponibilite FROM vehicule WHERE id = ?";
        
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
        String sql = "SELECT id, immatriculation, capacite, carburant, heure_disponibilite " +
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
        String sql = "SELECT v.id, v.immatriculation, v.capacite, v.carburant, v.heure_disponibilite " +
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

    private Vehicule mapVehicule(ResultSet rs) throws SQLException {
        Vehicule v = new Vehicule(
            rs.getInt("id"),
            rs.getString("immatriculation"),
            rs.getInt("capacite"),
            rs.getString("carburant").charAt(0)
        );
        v.setHeureDisponibilite(rs.getTime("heure_disponibilite"));
        return v;
    }
}
