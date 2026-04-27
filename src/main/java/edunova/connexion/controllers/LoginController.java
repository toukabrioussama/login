package edunova.connexion.controllers;

import edunova.connexion.tools.DatabaseConnection;
import edunova.connexion.tools.PasswordUtils;
import edunova.connexion.tools.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField        txtEmailO;
    @FXML private PasswordField    txtPasswordO;
    @FXML private ComboBox<String> cbRoleO;

    @FXML
    public void initialize() {
        cbRoleO.getItems().addAll("Administrateur", "Enseignant", "Etudiant");
    }

    @FXML
    private void handleLogin() {
        String email    = txtEmailO.getText().trim();
        String password = txtPasswordO.getText();
        String role     = cbRoleO.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir tous les champs.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT u.id_u, u.password_u, u.nom_u, u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND r.nom_r = ? AND u.actif_u = 1";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_u");

                if (PasswordUtils.verify(password, storedHash)) {

                    // Sauvegarder la session
                    SessionManager session = SessionManager.getInstance();
                    session.setUserId(rs.getInt("id_u"));
                    session.setEmail(email);
                    session.setRole(role);

                    // Enregistrer dans login_history
                    enregistrerHistorique(conn, rs.getInt("id_u"), true);
                 //TODO
                    // Ouvrir le dashboard
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/views/dashboard.fxml"));
                    Stage stage = new Stage();
                    stage.setTitle("EduNova - Dashboard");
                    stage.setScene(new Scene(loader.load(), 1100, 700));
                    stage.show();
                    ((Stage) txtEmailO.getScene().getWindow()).close();

                } else {
                    enregistrerHistorique(conn, rs.getInt("id_u"), false);
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Mot de passe incorrect.");
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Aucun compte trouvé avec cet email et ce rôle.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données",
                    "Impossible de se connecter :\n" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre :\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enregistrerHistorique(Connection conn,
                                       int userId, boolean succes) {
        try {
            String sql =
                    "INSERT INTO login_history (user_id, adresse_ip_lh, succes_lh) " +
                            "VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, "127.0.0.1");
            stmt.setBoolean(3, succes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur historique : " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/forgot_password.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EduNova - Mot de passe oublié");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            // Bloquer la fenêtre login tant que celle-ci est ouverte
            stage.initOwner(txtEmailO.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir la fenêtre : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}