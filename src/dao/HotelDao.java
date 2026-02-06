package dao;

import config.DbConfig;
import model.Hotel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HotelDao {
    public List<Hotel> findAll() throws Exception {
        String sql = "SELECT id_hotel, nom, adresse FROM Hotel ORDER BY nom";
        List<Hotel> hotels = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Hotel hotel = new Hotel(
                    rs.getInt("id_hotel"),
                    rs.getString("nom"),
                    rs.getString("adresse")
                );
                hotels.add(hotel);
            }
        }
        return hotels;
    }
}
