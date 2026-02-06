package dao;

import config.DbConfig;
import model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {
    public void insert(String idClient, int nbpassagers, Timestamp dateheure, int idHotel) throws Exception {
        String sql = "INSERT INTO Reservation (id_client, nbpassagers, dateheure, id_hotel) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idClient);
            ps.setInt(2, nbpassagers);
            ps.setTimestamp(3, dateheure);
            ps.setInt(4, idHotel);
            ps.executeUpdate();
        }
    }

    public List<Reservation> findAll() throws Exception {
        String sql = "SELECT r.id, r.id_client, r.nbpassagers, r.dateheure, r.id_hotel, h.nom AS hotel_nom "
                   + "FROM Reservation r JOIN Hotel h ON r.id_hotel = h.id_hotel "
                   + "ORDER BY r.dateheure DESC";
        List<Reservation> reservations = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reservation reservation = new Reservation(
                    rs.getInt("id"),
                    rs.getString("id_client"),
                    rs.getInt("nbpassagers"),
                    rs.getTimestamp("dateheure"),
                    rs.getInt("id_hotel"),
                    rs.getString("hotel_nom")
                );
                reservations.add(reservation);
            }
        }
        return reservations;
    }
}
