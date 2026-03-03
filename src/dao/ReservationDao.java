package dao;

import config.DbConfig;
import model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {

    /**
     * Insère une nouvelle réservation
     */
    public void insert(String idClient, int nombrePassagers, Timestamp dateHeureDepart, int idLieuDestination) throws SQLException {
        String sql = "INSERT INTO reservation (id_client, nombre_passagers, date_heure_depart, id_lieu_destination) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idClient);
            ps.setInt(2, nombrePassagers);
            ps.setTimestamp(3, dateHeureDepart);
            ps.setInt(4, idLieuDestination);
            ps.executeUpdate();
        }
    }

    /**
     * Trouve toutes les réservations avec les détails des lieux
     */
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT r.id, r.id_client, r.nombre_passagers, r.date_heure_depart, " +
                     "r.id_lieu_destination, l.libelle AS lieu_destination, aer.libelle AS lieu_depart " +
                     "FROM reservation r " +
                     "JOIN lieu l ON r.id_lieu_destination = l.id " +
                     "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                     "ORDER BY r.date_heure_depart DESC";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapReservation(rs));
            }
        }
        return reservations;
    }

    /**
     * Trouve les réservations pour une date donnée
     */
    public List<Reservation> findByDate(Date date) throws SQLException {
        String sql = "SELECT r.id, r.id_client, r.nombre_passagers, r.date_heure_depart, " +
                     "r.id_lieu_destination, l.libelle AS lieu_destination, aer.libelle AS lieu_depart " +
                     "FROM reservation r " +
                     "JOIN lieu l ON r.id_lieu_destination = l.id " +
                     "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                     "WHERE DATE(r.date_heure_depart) = ? " +
                     "ORDER BY r.date_heure_depart ASC";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapReservation(rs));
                }
            }
        }
        return reservations;
    }

    /**
     * Trouve les réservations non affectées pour une date donnée
     */
    public List<Reservation> findNonAffecteesByDate(Date date) throws SQLException {
        String sql = "SELECT r.id, r.id_client, r.nombre_passagers, r.date_heure_depart, " +
                     "r.id_lieu_destination, l.libelle AS lieu_destination, aer.libelle AS lieu_depart " +
                     "FROM reservation r " +
                     "JOIN lieu l ON r.id_lieu_destination = l.id " +
                     "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                     "WHERE DATE(r.date_heure_depart) = ? " +
                     "AND r.id NOT IN (SELECT id_reservation FROM affectation) " +
                     "ORDER BY r.date_heure_depart ASC";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapReservation(rs));
                }
            }
        }
        return reservations;
    }

    /**
     * Trouve une réservation par son ID
     */
    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT r.id, r.id_client, r.nombre_passagers, r.date_heure_depart, " +
                     "r.id_lieu_destination, l.libelle AS lieu_destination, aer.libelle AS lieu_depart " +
                     "FROM reservation r " +
                     "JOIN lieu l ON r.id_lieu_destination = l.id " +
                     "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                     "WHERE r.id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }
        return null;
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation(
            rs.getInt("id"),
            rs.getString("id_client"),
            rs.getInt("nombre_passagers"),
            rs.getTimestamp("date_heure_depart"),
            rs.getInt("id_lieu_destination")
        );
        r.setLieuDestination(rs.getString("lieu_destination"));
        r.setLieuDepart(rs.getString("lieu_depart"));
        return r;
    }
}
