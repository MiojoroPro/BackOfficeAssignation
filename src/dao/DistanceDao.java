package dao;

import config.DbConfig;
import model.Distance;
import java.sql.*;

public class DistanceDao {

    /**
     * Trouve la distance entre deux lieux
     */
    public Distance findByLieux(int fromId, int toId) throws SQLException {
        String sql = "SELECT id, from_id, to_id, km FROM distance WHERE from_id = ? AND to_id = ?";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, fromId);
            ps.setInt(2, toId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Distance(
                        rs.getInt("id"),
                        rs.getInt("from_id"),
                        rs.getInt("to_id"),
                        rs.getDouble("km")
                    );
                }
            }
            return null;
        }
    }

    /**
     * Retourne la distance en km entre deux lieux
     */
    public double getDistanceKm(int fromId, int toId) throws SQLException {
        Distance d = findByLieux(fromId, toId);
        return d != null ? d.getKm() : 0;
    }

    /**
     * Calcule la distance totale aller-retour entre l'aéroport et un lieu
     */
    public double getDistanceAllerRetour(int aeroportId, int lieuId) throws SQLException {
        double aller = getDistanceKm(aeroportId, lieuId);
        double retour = getDistanceKm(lieuId, aeroportId);
        return aller + retour;
    }
}
