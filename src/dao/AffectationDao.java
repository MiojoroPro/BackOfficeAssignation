package dao;

import config.DbConfig;
import model.AffectationDetails;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AffectationDao {

    /**
     * Insère une nouvelle affectation
     */
    public void insert(
        int idVehicule,
        int idReservation,
        Timestamp dateHeureDepart,
        Timestamp dateHeureRetour,
        int ordreLivraison,
        int nombrePassagersAffectes
    ) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            boolean hasPassagersCol = hasNombrePassagersAffectesColumn(conn);
            String sql;
            if (hasPassagersCol) {
                sql = "INSERT INTO affectation (id_vehicule, id_reservation, date_heure_depart, date_heure_retour, ordre_livraison, nombre_passagers_affectes) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                sql = "INSERT INTO affectation (id_vehicule, id_reservation, date_heure_depart, date_heure_retour, ordre_livraison) VALUES (?, ?, ?, ?, ?)";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idVehicule);
                ps.setInt(2, idReservation);
                ps.setTimestamp(3, dateHeureDepart);
                ps.setTimestamp(4, dateHeureRetour);
                ps.setInt(5, ordreLivraison);
                if (hasPassagersCol) {
                    ps.setInt(6, nombrePassagersAffectes);
                }
                ps.executeUpdate();
            }
        }
    }

    /**
     * Met à jour une affectation existante
     */
    public void update(
        int idAffectation,
        int idVehicule,
        int idReservation,
        Timestamp dateHeureDepart,
        Timestamp dateHeureRetour,
        int ordreLivraison,
        int nombrePassagersAffectes
    ) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            boolean hasPassagersCol = hasNombrePassagersAffectesColumn(conn);
            String sql;
            if (hasPassagersCol) {
                sql = "UPDATE affectation " +
                      "SET id_vehicule = ?, id_reservation = ?, date_heure_depart = ?, date_heure_retour = ?, ordre_livraison = ?, nombre_passagers_affectes = ? " +
                      "WHERE id = ?";
            } else {
                sql = "UPDATE affectation " +
                      "SET id_vehicule = ?, id_reservation = ?, date_heure_depart = ?, date_heure_retour = ?, ordre_livraison = ? " +
                      "WHERE id = ?";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idVehicule);
                ps.setInt(2, idReservation);
                ps.setTimestamp(3, dateHeureDepart);
                ps.setTimestamp(4, dateHeureRetour);
                ps.setInt(5, ordreLivraison);
                if (hasPassagersCol) {
                    ps.setInt(6, nombrePassagersAffectes);
                    ps.setInt(7, idAffectation);
                } else {
                    ps.setInt(6, idAffectation);
                }
                ps.executeUpdate();
            }
        }
    }

    /**
     * Supprime une affectation par son ID
     */
    public void deleteById(int idAffectation) throws SQLException {
        String sql = "DELETE FROM affectation WHERE id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAffectation);
            ps.executeUpdate();
        }
    }

    /**
     * Supprime toutes les affectations pour une date donnée
     */
    public void deleteByDate(Date date) throws SQLException {
        String sql = "DELETE FROM affectation WHERE DATE(date_heure_depart) = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, date);
            ps.executeUpdate();
        }
    }

    /**
     * Trouve toutes les affectations avec détails complets
     */
    public List<AffectationDetails> findAllDetails() throws SQLException {
        List<AffectationDetails> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection()) {
            boolean hasPassagersCol = hasNombrePassagersAffectesColumn(conn);
            String passagersExpr = hasPassagersCol ? "a.nombre_passagers_affectes" : "r.nombre_passagers";

            String sql = "SELECT a.id as id_affectation, v.id as id_vehicule, v.immatriculation, v.capacite, v.carburant, " +
                         "r.id as id_reservation, r.id_client, " + passagersExpr + " as nombre_passagers_affectes, " +
                         "aer.libelle as lieu_depart, l.libelle as lieu_arrivee, " +
                         "a.date_heure_depart, a.date_heure_retour, a.ordre_livraison " +
                         "FROM affectation a " +
                         "JOIN vehicule v ON a.id_vehicule = v.id " +
                         "JOIN reservation r ON a.id_reservation = r.id " +
                         "JOIN lieu l ON r.id_lieu_destination = l.id " +
                         "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                         "ORDER BY a.date_heure_depart ASC, a.ordre_livraison ASC";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAffectationDetails(rs));
                }
            }
        }
        return list;
    }

    /**
     * Trouve les affectations pour une date donnée avec détails complets
     */
    public List<AffectationDetails> findDetailsByDate(Date date) throws SQLException {
        List<AffectationDetails> list = new ArrayList<>();
        try (Connection conn = DbConfig.getConnection()) {
            boolean hasPassagersCol = hasNombrePassagersAffectesColumn(conn);
            String passagersExpr = hasPassagersCol ? "a.nombre_passagers_affectes" : "r.nombre_passagers";

            String sql = "SELECT a.id as id_affectation, v.id as id_vehicule, v.immatriculation, v.capacite, v.carburant, " +
                         "r.id as id_reservation, r.id_client, " + passagersExpr + " as nombre_passagers_affectes, " +
                         "aer.libelle as lieu_depart, l.libelle as lieu_arrivee, " +
                         "a.date_heure_depart, a.date_heure_retour, a.ordre_livraison " +
                         "FROM affectation a " +
                         "JOIN vehicule v ON a.id_vehicule = v.id " +
                         "JOIN reservation r ON a.id_reservation = r.id " +
                         "JOIN lieu l ON r.id_lieu_destination = l.id " +
                         "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                         "WHERE DATE(a.date_heure_depart) = ? " +
                         "ORDER BY v.immatriculation, a.date_heure_depart ASC, a.ordre_livraison ASC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, date);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapAffectationDetails(rs));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Trouve une affectation avec détails complets par son ID
     */
    public AffectationDetails findDetailById(int idAffectation) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            boolean hasPassagersCol = hasNombrePassagersAffectesColumn(conn);
            String passagersExpr = hasPassagersCol ? "a.nombre_passagers_affectes" : "r.nombre_passagers";

            String sql = "SELECT a.id as id_affectation, v.id as id_vehicule, v.immatriculation, v.capacite, v.carburant, " +
                         "r.id as id_reservation, r.id_client, " + passagersExpr + " as nombre_passagers_affectes, " +
                         "aer.libelle as lieu_depart, l.libelle as lieu_arrivee, " +
                         "a.date_heure_depart, a.date_heure_retour, a.ordre_livraison " +
                         "FROM affectation a " +
                         "JOIN vehicule v ON a.id_vehicule = v.id " +
                         "JOIN reservation r ON a.id_reservation = r.id " +
                         "JOIN lieu l ON r.id_lieu_destination = l.id " +
                         "CROSS JOIN (SELECT libelle FROM lieu WHERE type = 'AEROPORT' LIMIT 1) aer " +
                         "WHERE a.id = ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idAffectation);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapAffectationDetails(rs);
                    }
                }
            }
        }
        return null;
    }

    private boolean hasNombrePassagersAffectesColumn(Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns " +
                     "WHERE table_schema = current_schema() " +
                     "AND table_name = 'affectation' " +
                     "AND column_name = 'nombre_passagers_affectes'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    /**
     * Groupe les affectations par véhicule pour une date donnée
     * Retourne une map: idVehicule -> liste des affectations
     */
    public List<AffectationDetails> findDetailsByDateGroupedByVehicule(Date date) throws SQLException {
        return findDetailsByDate(date);
    }

    /**
     * Vérifie si une réservation est déjà affectée
     */
    public boolean isReservationAffectee(int idReservation) throws SQLException {
        String sql = "SELECT COUNT(*) FROM affectation WHERE id_reservation = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReservation);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private AffectationDetails mapAffectationDetails(ResultSet rs) throws SQLException {
        AffectationDetails ad = new AffectationDetails();
        ad.setIdAffectation(rs.getInt("id_affectation"));
        ad.setIdVehicule(rs.getInt("id_vehicule"));
        ad.setImmatriculation(rs.getString("immatriculation"));
        ad.setCapacite(rs.getInt("capacite"));
        ad.setCarburant(rs.getString("carburant").charAt(0));
        ad.setIdReservation(rs.getInt("id_reservation"));
        ad.setIdClient(rs.getString("id_client"));
        ad.setNombrePassagers(rs.getInt("nombre_passagers_affectes"));
        ad.setLieuDepart(rs.getString("lieu_depart"));
        ad.setLieuArrivee(rs.getString("lieu_arrivee"));
        ad.setDateHeureDepart(rs.getTimestamp("date_heure_depart"));
        ad.setDateHeureRetour(rs.getTimestamp("date_heure_retour"));
        ad.setOrdreLivraison(rs.getInt("ordre_livraison"));
        return ad;
    }
}
