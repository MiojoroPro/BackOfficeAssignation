package dao;

import config.DbConfig;
import model.Parametre;
import java.sql.*;

public class ParametreDao {

    public Parametre getParametre() throws SQLException {
        String sql = "SELECT id, vitesse_moyenne, temps_attente FROM parametre LIMIT 1";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return new Parametre(
                    rs.getInt("id"),
                    rs.getDouble("vitesse_moyenne"),
                    rs.getInt("temps_attente")
                );
            }
            return null;
        }
    }

    public void update(Parametre parametre) throws SQLException {
        String sql = "UPDATE parametre SET vitesse_moyenne = ?, temps_attente = ? WHERE id = ?";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, parametre.getVitesseMoyenne());
            ps.setInt(2, parametre.getTempsAttente());
            ps.setInt(3, parametre.getId());
            ps.executeUpdate();
        }
    }
}
