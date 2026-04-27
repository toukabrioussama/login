package edunova.connexion.tests;

import edunova.connexion.tools.DatabaseConnection;
import edunova.connexion.tools.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestConnexion {

    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("   TEST DE CONNEXION - EDUNOVA");
        System.out.println("===========================================\n");

        // ── TEST 1 : Connexion à la base ──────────────────────────
        System.out.println("📡 TEST 1 : Connexion à MySQL...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Connexion réussie !\n");
        } catch (SQLException e) {
            System.out.println("❌ ÉCHEC connexion : " + e.getMessage());
            return; // inutile de continuer si la BD est inaccessible
        }

        // ── TEST 2 : Lire les rôles ───────────────────────────────
        System.out.println("📋 TEST 2 : Lecture de la table ROLE...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM role");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("   → id=" + rs.getInt("id_r")
                        + " | nom=" + rs.getString("nom_r")
                        + " | desc=" + rs.getString("description_r"));
            }
            System.out.println("✅ Rôles lus avec succès !\n");
        } catch (SQLException e) {
            System.out.println("❌ ÉCHEC lecture roles : " + e.getMessage() + "\n");
        }

        // ── TEST 3 : Lire les utilisateurs ────────────────────────
        System.out.println("👥 TEST 3 : Lecture de la table USER...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT u.id_u, u.email_u, u.nom_u, u.prenom_u,
                       u.actif_u, r.nom_r
                FROM user u
                JOIN role r ON u.role_id = r.id_r
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("   → [" + rs.getString("nom_r") + "] "
                        + rs.getString("prenom_u") + " " + rs.getString("nom_u")
                        + " | email=" + rs.getString("email_u")
                        + " | actif=" + (rs.getBoolean("actif_u") ? "OUI" : "NON"));
            }
            System.out.println("✅ Utilisateurs lus avec succès !\n");
        } catch (SQLException e) {
            System.out.println("❌ ÉCHEC lecture users : " + e.getMessage() + "\n");
        }

        // ── TEST 4 : Simulation du login ──────────────────────────
        System.out.println("🔐 TEST 4 : Simulation du login...");
        testerLogin("admin@edunova.com",    "admin123",  "Administrateur");
        testerLogin("prof@edunova.com",     "admin123",  "Enseignant");
        testerLogin("etudiant@edunova.com", "admin123",  "Etudiant");
        testerLogin("admin@edunova.com",    "MAUVAIS",   "Administrateur"); // doit échouer
        testerLogin("inconnu@test.com",     "admin123",  "Administrateur"); // doit échouer

        // ── TEST 5 : Enregistrement dans login_history ────────────
        System.out.println("📝 TEST 5 : Enregistrement dans login_history...");
        enregistrerHistorique(1, "127.0.0.1", true);
        enregistrerHistorique(1, "192.168.1.1", false);
        lireHistorique();

        System.out.println("===========================================");
        System.out.println("   TOUS LES TESTS TERMINÉS");
        System.out.println("===========================================");
    }

    // ── Méthode : tester un login ─────────────────────────────────
    private static void testerLogin(String email, String motDePasse, String role) {
        System.out.print("   → Login [" + email + " / " + role + "] : ");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT u.id_u, u.password_u, u.nom_u
                FROM user u
                JOIN role r ON u.role_id = r.id_r
                WHERE u.email_u = ?
                  AND r.nom_r   = ?
                  AND u.actif_u = 1
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashStocke = rs.getString("password_u");
                if (PasswordUtils.verify(motDePasse, hashStocke)) {
                    System.out.println("✅ SUCCÈS - Bonjour " + rs.getString("nom_u"));
                    enregistrerHistorique(rs.getInt("id_u"), "127.0.0.1", true);
                } else {
                    System.out.println("❌ ÉCHEC - Mot de passe incorrect");
                    enregistrerHistorique(rs.getInt("id_u"), "127.0.0.1", false);
                }
            } else {
                System.out.println("❌ ÉCHEC - Utilisateur/rôle introuvable");
            }

        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL : " + e.getMessage());
        }
    }

    // ── Méthode : enregistrer dans login_history ──────────────────
    private static void enregistrerHistorique(int userId, String ip, boolean succes) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                INSERT INTO login_history (user_id, adresse_ip_lh, succes_lh)
                VALUES (?, ?, ?)
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, ip);
            stmt.setBoolean(3, succes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("     ⚠️ Erreur historique : " + e.getMessage());
        }
    }

    // ── Méthode : lire login_history ──────────────────────────────
    private static void lireHistorique() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT lh.id_lh, u.email_u, lh.adresse_ip_lh,
                       lh.succes_lh, lh.date_connexion_lh
                FROM login_history lh
                JOIN user u ON lh.user_id = u.id_u
                ORDER BY lh.date_connexion_lh DESC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            System.out.println("   Historique des connexions :");
            while (rs.next()) {
                System.out.println("   → " + rs.getString("date_connexion_lh")
                        + " | " + rs.getString("email_u")
                        + " | IP=" + rs.getString("adresse_ip_lh")
                        + " | " + (rs.getBoolean("succes_lh") ? "✅ SUCCÈS" : "❌ ÉCHEC"));
            }
            System.out.println("✅ Historique lu avec succès !\n");
        } catch (SQLException e) {
            System.out.println("❌ ÉCHEC lecture historique : " + e.getMessage() + "\n");
        }
    }
}