package edunova.connexion.controllers;

import edunova.connexion.tools.DatabaseConnection;
import edunova.connexion.tools.PasswordUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForgotPasswordController {

    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtNouveauMdp;
    @FXML private PasswordField txtConfirmMdp;
    @FXML private Label         lblEmailConfirm;
    @FXML private VBox          etape1;
    @FXML private VBox          etape2;

    // Stocker l'id de l'utilisateur trouvé
    private int userIdTrouve = -1;

    // ── ETAPE 1 : Vérifier que l'email existe ────────────────────
    @FXML
    private void handleVerifierEmail() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Champ manquant", "Veuillez entrer votre adresse email.");
            return;
        }

        if (!email.contains("@")) {
            showAlert(Alert.AlertType.WARNING,
                    "Email invalide", "Veuillez entrer un email valide.");
            return;
        }

        // Chercher l'email dans la base
        String sql = "SELECT id_u, nom_u, prenom_u FROM user WHERE email_u = ? AND actif_u = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Email trouvé → passer à l'étape 2
                userIdTrouve = rs.getInt("id_u");
                String nom = rs.getString("prenom_u") + " " + rs.getString("nom_u");

                lblEmailConfirm.setText("Compte trouvé : " + nom);

                // Masquer étape 1, afficher étape 2
                etape1.setVisible(false);
                etape1.setManaged(false);
                etape2.setVisible(true);
                etape2.setManaged(true);

            } else {
                showAlert(Alert.AlertType.ERROR,
                        "Email introuvable",
                        "Aucun compte actif trouvé avec cet email.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "Erreur base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── ETAPE 2 : Réinitialiser le mot de passe ──────────────────
    @FXML
    private void handleReinitialiser() {
        String nouveauMdp = txtNouveauMdp.getText();
        String confirmMdp = txtConfirmMdp.getText();

        // Validation
        if (nouveauMdp.isEmpty() || confirmMdp.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "Champs manquants", "Veuillez remplir les deux champs.");
            return;
        }

        if (nouveauMdp.length() < 6) {
            showAlert(Alert.AlertType.WARNING,
                    "Mot de passe trop court",
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!nouveauMdp.equals(confirmMdp)) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "Les deux mots de passe ne correspondent pas.");
            txtNouveauMdp.clear();
            txtConfirmMdp.clear();
            return;
        }

        // Mettre à jour dans la base
        String sql = "UPDATE user SET password_u = ? WHERE id_u = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtils.hash(nouveauMdp));
            stmt.setInt(2, userIdTrouve);

            if (stmt.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Succès",
                        "Votre mot de passe a été réinitialisé avec succès !\n" +
                                "Vous pouvez maintenant vous connecter.");

                // Fermer la fenêtre
                fermerFenetre();

            } else {
                showAlert(Alert.AlertType.ERROR,
                        "Erreur", "Échec de la réinitialisation.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "Erreur base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Retour à la connexion ─────────────────────────────────────
    @FXML
    private void handleRetour() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) txtEmail.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
