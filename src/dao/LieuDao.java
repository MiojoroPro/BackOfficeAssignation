package dao;

import config.DbConfig;
import model.Lieu;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LieuDao {

    public List<Lieu> findAll() throws SQLException {
        List<Lieu> lieux = new ArrayList<>();
        String sql = "SELECT id, code, libelle, type FROM lieu ORDER BY type, code";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lieux.add(new Lieu(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("libelle"),
                    rs.getString("type")
                ));
            }
        }
        return lieux;
    }

    public List<Lieu> findHotels() throws SQLException {
        List<Lieu> lieux = new ArrayList<>();
        String sql = "SELECT id, code, libelle, type FROM lieu WHERE type = 'HOTEL' ORDER BY code";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lieux.add(new Lieu(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("libelle"),
                    rs.getString("type")
                ));
            }
        }
        return lieux;
    }

    public Lieu findAeroport() throws SQLException {
        String sql = "SELECT id, code, libelle, type FROM lieu WHERE type = 'AEROPORT' LIMIT 1";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return new Lieu(
                    rs.getInt("id"),
                    rs.getString("code"),
                    rs.getString("libelle"),
                    rs.getString("type")
                );
            }
            return null;
        }
    }

    public Lieu findById(int id) throws SQLException {
        String sql = "SELECT id, code, libelle, type FROM lieu WHERE id = ?";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Lieu(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("libelle"),
                        rs.getString("type")
                    );
                }
            }
            return null;
        }
    }
}
