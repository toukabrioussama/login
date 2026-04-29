package edunova.connexion.controllers;

import edunova.connexion.tools.DatabaseConnection;
import edunova.connexion.tools.PasswordUtils;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForgotPasswordController {


    @FXML private VBox      etape1;
    @FXML private TextField txtEmail;
    @FXML private Label     errEmail;


    @FXML private VBox          etape2;
    @FXML private PasswordField txtNouveauMdp;
    @FXML private PasswordField txtConfirmMdp;
    @FXML private Label         lblEmailConfirm;
    @FXML private Label         errNouveauMdp;
    @FXML private Label         errConfirmMdp;

    // Indicateur étapes
    @FXML private StackPane etapeCircle2;
    @FXML private Label     lblEtape2Txt;

    // Jauge force MDP
    @FXML private Label  lblForceMdp;
    @FXML private Region force1;
    @FXML private Region force2;
    @FXML private Region force3;
    @FXML private Region force4;
    @FXML private Label lblEtape2Num;

    private int userIdTrouve = -1;

    // ── Initialisation ────────────────────────────────────────────
    @FXML
    public void initialize() {

        txtEmail.textProperty().addListener((obs, o, n) -> {
            if (!n.isEmpty()) validerEmail();
            else errEmail.setText("");
        });


        txtNouveauMdp.textProperty().addListener((obs, o, n) -> {
            mettreAJourForceMdp(n);
            if (!txtConfirmMdp.getText().isEmpty())
                validerConfirmMdp();
        });

        txtConfirmMdp.textProperty().addListener((obs, o, n) -> {
            if (!n.isEmpty()) validerConfirmMdp();
            else errConfirmMdp.setText("");
        });
    }


    //   Vérifier l'email


    @FXML
    private void handleVerifierEmail() {
        if (!validerEmail()) return;

        String email = txtEmail.getText().trim();
        String sql =
                "SELECT id_u, nom_u, prenom_u FROM user " +
                        "WHERE email_u = ? AND actif_u = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userIdTrouve = rs.getInt("id_u");
                String nom = rs.getString("prenom_u") + " "
                        + rs.getString("nom_u");

                lblEmailConfirm.setText(nom + " · " + email);


                etapeCircle2.setStyle(
                        "-fx-background-color: #7c3aed;" +
                                "-fx-background-radius: 50;" +
                                "-fx-min-width: 28; -fx-min-height: 28;" +
                                "-fx-max-width: 28; -fx-max-height: 28;");
                lblEtape2Txt.setStyle(
                        "-fx-font-size: 11; -fx-text-fill: #7c3aed;" +
                                "-fx-font-weight: bold;");


                etape1.setVisible(false);
                etape1.setManaged(false);
                etape2.setVisible(true);
                etape2.setManaged(true);

            } else {
                setErreur(txtEmail, errEmail,
                        "Aucun compte actif trouvé avec cet email.");
            }

        } catch (SQLException e) {
            setErreur(txtEmail, errEmail,
                    "Erreur de connexion. Réessayez.");
            e.printStackTrace();
        }
    }

    // Réinitialiser le mot de passe


    @FXML
    private void handleReinitialiser() {
        boolean ok1 = validerNouveauMdp();
        boolean ok2 = validerConfirmMdp();
        if (!ok1 || !ok2) return;

        String sql = "UPDATE user SET password_u = ? WHERE id_u = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtils.hash(txtNouveauMdp.getText()));
            stmt.setInt(2, userIdTrouve);

            if (stmt.executeUpdate() > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText(
                        "✅ Mot de passe réinitialisé avec succès !\n" +
                                "Vous pouvez maintenant vous connecter.");
                alert.showAndWait();
                fermerFenetre();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Échec de la réinitialisation.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }


    //  VALIDATIONS


    private boolean validerEmail() {
        String v = txtEmail.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtEmail, errEmail, "L'email est obligatoire.");
            return false;
        }
        if (!v.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setErreur(txtEmail, errEmail,
                    "Format invalide. Ex: nom@domaine.com");
            return false;
        }
        setOk(txtEmail, errEmail);
        return true;
    }

    private boolean validerNouveauMdp() {
        String v = txtNouveauMdp.getText();
        if (v.isEmpty()) {
            setErreur(txtNouveauMdp, errNouveauMdp,
                    "Le mot de passe est obligatoire.");
            return false;
        }
        if (v.length() < 6) {
            setErreur(txtNouveauMdp, errNouveauMdp,
                    "Minimum 6 caractères requis.");
            return false;
        }
        setOk(txtNouveauMdp, errNouveauMdp);
        return true;
    }

    private boolean validerConfirmMdp() {
        String v1 = txtNouveauMdp.getText();
        String v2 = txtConfirmMdp.getText();
        if (v2.isEmpty()) {
            setErreur(txtConfirmMdp, errConfirmMdp,
                    "Veuillez confirmer le mot de passe.");
            return false;
        }
        if (!v1.equals(v2)) {
            setErreur(txtConfirmMdp, errConfirmMdp,
                    "Les mots de passe ne correspondent pas.");
            return false;
        }
        setOk(txtConfirmMdp, errConfirmMdp);
        return true;
    }


    //  JAUGE FORCE MOT DE PASSE


    private void mettreAJourForceMdp(String mdp) {
        if (mdp.isEmpty()) {
            resetForce();
            lblForceMdp.setText("");
            return;
        }

        int score = calculerScore(mdp);

        String vide   = "-fx-background-color: #2d2d4e; -fx-background-radius: 2;";
        String rouge  = "-fx-background-color: #ef4444; -fx-background-radius: 2;";
        String orange = "-fx-background-color: #f97316; -fx-background-radius: 2;";
        String jaune  = "-fx-background-color: #eab308; -fx-background-radius: 2;";
        String vert   = "-fx-background-color: #22c55e; -fx-background-radius: 2;";

        switch (score) {
            case 1 -> {
                force1.setStyle(rouge);
                force2.setStyle(vide);
                force3.setStyle(vide);
                force4.setStyle(vide);
                lblForceMdp.setText("Très faible");
                lblForceMdp.setStyle(
                        "-fx-font-size: 11; -fx-text-fill: #ef4444;");
            }
            case 2 -> {
                force1.setStyle(orange);
                force2.setStyle(orange);
                force3.setStyle(vide);
                force4.setStyle(vide);
                lblForceMdp.setText("Faible");
                lblForceMdp.setStyle(
                        "-fx-font-size: 11; -fx-text-fill: #f97316;");
            }
            case 3 -> {
                force1.setStyle(jaune);
                force2.setStyle(jaune);
                force3.setStyle(jaune);
                force4.setStyle(vide);
                lblForceMdp.setText("Moyen");
                lblForceMdp.setStyle(
                        "-fx-font-size: 11; -fx-text-fill: #eab308;");
            }
            case 4 -> {
                force1.setStyle(vert);
                force2.setStyle(vert);
                force3.setStyle(vert);
                force4.setStyle(vert);
                lblForceMdp.setText("Fort ✓");
                lblForceMdp.setStyle(
                        "-fx-font-size: 11; -fx-text-fill: #22c55e;");
            }
            default -> {
                resetForce();
                lblForceMdp.setText("");
            }
        }
    }

    private int calculerScore(String mdp) {
        int score = 0;
        if (mdp.length() >= 6)                          score++;
        if (mdp.length() >= 10)                         score++;
        if (mdp.matches(".*[A-Z].*") &&
                mdp.matches(".*[a-z].*"))                   score++;
        if (mdp.matches(".*[0-9].*") &&
                mdp.matches(".*[!@#$%^&*()_+\\-=].*"))     score++;
        return score;
    }

    private void resetForce() {
        String vide = "-fx-background-color: #2d2d4e; -fx-background-radius: 2;";
        force1.setStyle(vide);
        force2.setStyle(vide);
        force3.setStyle(vide);
        force4.setStyle(vide);
    }


    //  HELPERS STYLE


    private void setErreur(Control champ, Label errLabel, String msg) {
        errLabel.setText("⚠ " + msg);
        champ.setStyle(champ.getStyle()
                .replace("-fx-border-color: #2d2d4e;", "")
                .replace("-fx-border-color: #22c55e;", "") +
                "-fx-border-color: #f87171;");
    }

    private void setOk(Control champ, Label errLabel) {
        errLabel.setText("");
        champ.setStyle(champ.getStyle()
                .replace("-fx-border-color: #2d2d4e;", "")
                .replace("-fx-border-color: #f87171;", "") +
                "-fx-border-color: #22c55e;");
    }


    @FXML
    private void handleRetour() { fermerFenetre(); }

    private void fermerFenetre() {
        ((Stage) txtEmail.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}