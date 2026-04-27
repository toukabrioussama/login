package edunova.connexion.dao;

import edunova.connexion.tools.DatabaseConnection;
import edunova.connexion.tools.PasswordUtils;
import edunova.connexion.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ── READ : tous les utilisateurs ─────────────────────────────
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql =
                "SELECT u.id_u, u.email_u, u.nom_u, u.prenom_u, " +
                        "       u.telephone_u, u.actif_u, u.role_id, r.nom_r " +
                        "FROM user u " +
                        "JOIN role r ON u.role_id = r.id_r " +
                        "ORDER BY u.nom_u, u.prenom_u";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id_u"),
                        rs.getString("email_u"),
                        rs.getString("nom_u"),
                        rs.getString("prenom_u"),
                        rs.getString("telephone_u"),
                        rs.getBoolean("actif_u"),
                        rs.getInt("role_id"),
                        rs.getString("nom_r")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── CREATE : ajouter un utilisateur ──────────────────────────
    public boolean insert(User u) {
        String sql =
                "INSERT INTO user (email_u, password_u, nom_u, prenom_u, " +
                        "                  telephone_u, actif_u, role_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getEmail());
            stmt.setString(2, PasswordUtils.hash(u.getPassword()));
            stmt.setString(3, u.getNom());
            stmt.setString(4, u.getPrenom());
            // Si téléphone vide → mettre NULL
            if (u.getTelephone() == null || u.getTelephone().isEmpty()) {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(5, u.getTelephone());
            }
            stmt.setBoolean(6, u.isActif());
            stmt.setInt(7, u.getRoleId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // Afficher l'erreur exacte dans la console
            System.out.println("ERREUR INSERT : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── UPDATE : modifier un utilisateur ─────────────────────────
    public boolean update(User u) {
        boolean changePassword = u.getPassword() != null
                && !u.getPassword().isEmpty();

        String sql = changePassword
                ? "UPDATE user SET email_u=?, nom_u=?, prenom_u=?, " +
                "telephone_u=?, actif_u=?, role_id=?, password_u=? " +
                "WHERE id_u=?"
                : "UPDATE user SET email_u=?, nom_u=?, prenom_u=?, " +
                "telephone_u=?, actif_u=?, role_id=? " +
                "WHERE id_u=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getEmail());
            stmt.setString(2, u.getNom());
            stmt.setString(3, u.getPrenom());
            stmt.setString(4, u.getTelephone());
            stmt.setBoolean(5, u.isActif());
            stmt.setInt(6, u.getRoleId());

            if (changePassword) {
                stmt.setString(7, PasswordUtils.hash(u.getPassword()));
                stmt.setInt(8, u.getId());
            } else {
                stmt.setInt(7, u.getId());
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── DELETE : supprimer un utilisateur ────────────────────────
    public boolean delete(int id) {
        String sql = "DELETE FROM user WHERE id_u = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── SEARCH : rechercher ───────────────────────────────────────
    public List<User> search(String keyword) {
        List<User> list = new ArrayList<>();
        String sql =
                "SELECT u.id_u, u.email_u, u.nom_u, u.prenom_u, " +
                        "       u.telephone_u, u.actif_u, u.role_id, r.nom_r " +
                        "FROM user u " +
                        "JOIN role r ON u.role_id = r.id_r " +
                        "WHERE u.nom_u LIKE ? OR u.prenom_u LIKE ? OR u.email_u LIKE ? " +
                        "ORDER BY u.nom_u";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String kw = "%" + keyword + "%";
            stmt.setString(1, kw);
            stmt.setString(2, kw);
            stmt.setString(3, kw);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new User(
                        rs.getInt("id_u"),
                        rs.getString("email_u"),
                        rs.getString("nom_u"),
                        rs.getString("prenom_u"),
                        rs.getString("telephone_u"),
                        rs.getBoolean("actif_u"),
                        rs.getInt("role_id"),
                        rs.getString("nom_r")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Récupérer les rôles pour le ComboBox ─────────────────────
    public List<String[]> findAllRoles() {
        List<String[]> roles = new ArrayList<>();
        String sql = "SELECT id_r, nom_r FROM role ORDER BY nom_r";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(new String[]{
                        rs.getString("id_r"),
                        rs.getString("nom_r")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }
}