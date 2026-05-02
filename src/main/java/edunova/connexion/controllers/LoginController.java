package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;
import edunova.connexion.tools.DatabaseConnection;
import edunova.connexion.tools.GoogleAuthService;
import edunova.connexion.tools.PasswordUtils;
import edunova.connexion.tools.SessionManager;

import com.google.api.services.oauth2.model.Userinfo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LoginController {

    // ── Connexion ─────────────────────────────────────────────────
    @FXML private VBox          panneauConnexion;
    @FXML private TextField     txtEmailO;
    @FXML private PasswordField txtPasswordO;
    @FXML private Label         errLoginEmail;
    @FXML private Label         errLoginPassword;

    // ── Captcha ───────────────────────────────────────────────────
    @FXML private Label     lblCaptchaQuestion;
    @FXML private TextField txtCaptcha;
    @FXML private Label     errCaptcha;

    // ── Inscription ───────────────────────────────────────────────
    @FXML private VBox             panneauInscription;
    @FXML private TextField        txtRegNom;
    @FXML private TextField        txtRegPrenom;
    @FXML private TextField        txtRegEmail;
    @FXML private TextField        txtRegTel;
    @FXML private PasswordField    txtRegPassword;
    @FXML private PasswordField    txtRegConfirm;
    @FXML private ComboBox<String> cbRegRole;
    @FXML private CheckBox         chkCgu;
    @FXML private Label            errRegNom;
    @FXML private Label            errRegPrenom;
    @FXML private Label            errRegEmail;
    @FXML private Label            errRegTel;
    @FXML private Label            errRegPassword;
    @FXML private Label            errRegConfirm;
    @FXML private Label            errRegRole;
    @FXML private Label            errCgu;

    private final UserDAO dao            = new UserDAO();
    private       int     captchaReponse = 0;

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        cbRegRole.getItems().addAll(
                "Administrateur", "Enseignant", "Etudiant");
        genererCaptcha();

        // Validations temps réel connexion
        txtEmailO.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerLoginEmail();
            else errLoginEmail.setText("");
        });
        txtPasswordO.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerLoginPassword();
            else errLoginPassword.setText("");
        });

        // Validations temps réel inscription
        txtRegNom.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegNom(); });
        txtRegPrenom.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegPrenom(); });
        txtRegEmail.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegEmail(); });
        txtRegTel.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegTel(); });
        txtRegPassword.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegPassword(); });
        txtRegConfirm.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegConfirm(); });
    }

    // ══════════════════════════════════════════════════════════════
    //  CAPTCHA
    // ══════════════════════════════════════════════════════════════

    private void genererCaptcha() {
        Random rand = new Random();
        int a    = rand.nextInt(10) + 1;
        int b    = rand.nextInt(10) + 1;
        int type = rand.nextInt(3);
        String question;
        switch (type) {
            case 0 -> {
                captchaReponse = a + b;
                question = "Combien font  " + a + " + " + b + " ?";
            }
            case 1 -> {
                captchaReponse = a * b;
                question = "Combien font  " + a + " × " + b + " ?";
            }
            default -> {
                int max = Math.max(a, b);
                int min = Math.min(a, b);
                captchaReponse = max - min;
                question = "Combien font  " + max + " − " + min + " ?";
            }
        }
        lblCaptchaQuestion.setText(question);
        txtCaptcha.clear();
        errCaptcha.setText("");
    }

    @FXML
    private void handleRefreshCaptcha() { genererCaptcha(); }

    private boolean validerCaptcha() {
        String rep = txtCaptcha.getText().trim();
        if (rep.isEmpty()) {
            errCaptcha.setText(
                    "⚠ Veuillez répondre à la question.");
            return false;
        }
        try {
            if (Integer.parseInt(rep) == captchaReponse) {
                errCaptcha.setText("");
                return true;
            } else {
                errCaptcha.setText(
                        "⚠ Réponse incorrecte. Réessayez.");
                genererCaptcha();
                return false;
            }
        } catch (NumberFormatException e) {
            errCaptcha.setText(
                    "⚠ Entrez uniquement un chiffre.");
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CONNEXION NORMALE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        boolean e = validerLoginEmail();
        boolean p = validerLoginPassword();
        boolean c = validerCaptcha();
        if (!e || !p || !c) return;

        String email    = txtEmailO.getText().trim();
        String password = txtPasswordO.getText();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql =
                    "SELECT u.id_u, u.password_u, " +
                            "       u.nom_u, u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND u.actif_u = 1";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (PasswordUtils.verify(
                        password, rs.getString("password_u"))) {

                    String role = rs.getString("nom_r");
                    SessionManager s = SessionManager.getInstance();
                    s.setUserId(rs.getInt("id_u"));
                    s.setEmail(email);
                    s.setRole(role);

                    enregistrerHistorique(conn,
                            rs.getInt("id_u"), true);
                    ouvrirDashboard();

                } else {
                    enregistrerHistorique(conn,
                            rs.getInt("id_u"), false);
                    setErreur(txtPasswordO, errLoginPassword,
                            "Mot de passe incorrect.");
                    genererCaptcha();
                }
            } else {
                setErreur(txtEmailO, errLoginEmail,
                        "Aucun compte actif trouvé.");
                genererCaptcha();
            }
        } catch (SQLException ex) {
            showAlert("Erreur BD : " + ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur : " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  CONNEXION GOOGLE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleGoogleLogin() {
        // Désactiver le bouton pendant le chargement
        showAlert("🔄 Ouverture du navigateur Google...\n\n" +
                "Une fenêtre va s'ouvrir dans votre navigateur.\n" +
                "Connectez-vous avec votre compte Google.");

        // Traitement en arrière-plan
        new Thread(() -> {
            try {
                // Récupérer les infos Google
                Userinfo userInfo =
                        GoogleAuthService.getGoogleUserInfo();

                String email  = userInfo.getEmail();
                String nom    = userInfo.getFamilyName() != null
                        ? userInfo.getFamilyName() : "";
                String prenom = userInfo.getGivenName() != null
                        ? userInfo.getGivenName() : "";

                Platform.runLater(() ->
                        traiterConnexionGoogle(
                                email, nom, prenom));

            } catch (Exception ex) {
                Platform.runLater(() ->
                        showAlert("❌ Erreur Google :\n" +
                                ex.getMessage()));
            }
        }).start();
    }

    private void traiterConnexionGoogle(
            String email, String nom, String prenom) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Chercher si l'utilisateur existe déjà
            String sql =
                    "SELECT u.id_u, u.nom_u, u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND u.actif_u = 1";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // ✅ Compte existant → connexion directe
                String role = rs.getString("nom_r");
                SessionManager s = SessionManager.getInstance();
                s.setUserId(rs.getInt("id_u"));
                s.setEmail(email);
                s.setRole(role);

                enregistrerHistorique(conn,
                        rs.getInt("id_u"), true);

                showAlert("✅ Connexion Google réussie !\n" +
                        "Bienvenue " + rs.getString("prenom_u") +
                        " " + rs.getString("nom_u"));
                ouvrirDashboard();

            } else {
                // ❌ Compte inexistant →
                // Pré-remplir le formulaire d'inscription
                showAlert("📋 Compte Google non trouvé.\n\n" +
                        "Veuillez compléter votre inscription.\n" +
                        "Vos informations ont été pré-remplies.");

                // Pré-remplir le formulaire inscription
                panneauConnexion.setVisible(false);
                panneauConnexion.setManaged(false);
                panneauInscription.setVisible(true);
                panneauInscription.setManaged(true);

                txtRegNom.setText(nom);
                txtRegPrenom.setText(prenom);
                txtRegEmail.setText(email);
                // Générer un mot de passe temporaire
                String mdpTemp = UUID.randomUUID()
                        .toString().substring(0, 8);
                txtRegPassword.setText(mdpTemp);
                txtRegConfirm.setText(mdpTemp);

                showAlert("🔑 Mot de passe temporaire généré :\n" +
                        mdpTemp + "\n\n" +
                        "Notez-le ou changez-le après connexion.");
            }

        } catch (SQLException ex) {
            showAlert("Erreur BD : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  INSCRIPTION
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleShowInscription() {
        panneauConnexion.setVisible(false);
        panneauConnexion.setManaged(false);
        panneauInscription.setVisible(true);
        panneauInscription.setManaged(true);
        effacerErreursInscription();
    }

    @FXML
    private void handleShowConnexion() {
        panneauInscription.setVisible(false);
        panneauInscription.setManaged(false);
        panneauConnexion.setVisible(true);
        panneauConnexion.setManaged(true);
        genererCaptcha();
    }

    @FXML
    private void handleInscription() {
        boolean n  = validerRegNom();
        boolean p  = validerRegPrenom();
        boolean e  = validerRegEmail();
        boolean t  = validerRegTel();
        boolean pw = validerRegPassword();
        boolean cf = validerRegConfirm();
        boolean r  = validerRegRole();
        boolean cg = validerCgu();
        if (!n || !p || !e || !t || !pw || !cf || !r || !cg)
            return;

        User u = new User();
        u.setNom(txtRegNom.getText().trim());
        u.setPrenom(txtRegPrenom.getText().trim());
        u.setEmail(txtRegEmail.getText().trim());
        u.setTelephone(txtRegTel.getText().trim());
        u.setPassword(txtRegPassword.getText());
        u.setActif(true);
        u.setRoleId(getRoleId(cbRegRole.getValue()));

        if (dao.insert(u)) {
            showAlert("✅ Compte créé avec succès !\n\n" +
                    "Bienvenue " + u.getPrenom() +
                    " " + u.getNom() + " !\n" +
                    "Vous pouvez maintenant vous connecter.");
            handleShowConnexion();
        } else {
            setErreurLabel(errRegEmail,
                    "Cet email est déjà utilisé.");
        }
    }

    // ── Historique ────────────────────────────────────────────────
    private void enregistrerHistorique(Connection conn,
                                       int userId, boolean succes) {
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO login_history " +
                            "(user_id, adresse_ip_lh, succes_lh) " +
                            "VALUES (?, ?, ?)");
            stmt.setInt(1, userId);
            stmt.setString(2, "127.0.0.1");
            stmt.setBoolean(3, succes);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(
                    "Erreur historique : " + e.getMessage());
        }
    }

    // ── Ouvrir Dashboard ──────────────────────────────────────────
    private void ouvrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/views/dashboard.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EduNova — Dashboard");
            stage.setScene(new Scene(
                    loader.load(), 1100, 700));
            stage.show();
            ((Stage) txtEmailO.getScene()
                    .getWindow()).close();
        } catch (Exception ex) {
            showAlert("Erreur ouverture dashboard : " +
                    ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ── Mot de passe oublié ───────────────────────────────────────
    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/views/forgot_password.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EduNova - Mot de passe oublié");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(
                    txtEmailO.getScene().getWindow());
            stage.initModality(
                    javafx.stage.Modality.WINDOW_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  VALIDATIONS CONNEXION
    // ══════════════════════════════════════════════════════════════

    private boolean validerLoginEmail() {
        String v = txtEmailO.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtEmailO, errLoginEmail,
                    "L'email est obligatoire.");
            return false;
        }
        if (!v.matches(
                "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setErreur(txtEmailO, errLoginEmail,
                    "Format invalide.");
            return false;
        }
        setOk(txtEmailO, errLoginEmail);
        return true;
    }

    private boolean validerLoginPassword() {
        String v = txtPasswordO.getText();
        if (v.isEmpty()) {
            setErreur(txtPasswordO, errLoginPassword,
                    "Le mot de passe est obligatoire.");
            return false;
        }
        setOk(txtPasswordO, errLoginPassword);
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  VALIDATIONS INSCRIPTION
    // ══════════════════════════════════════════════════════════════

    private boolean validerRegNom() {
        String v = txtRegNom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegNom, errRegNom,
                    "Obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtRegNom, errRegNom,
                    "Lettres uniquement (2-50 car.)");
            return false;
        }
        setOk(txtRegNom, errRegNom);
        return true;
    }

    private boolean validerRegPrenom() {
        String v = txtRegPrenom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegPrenom, errRegPrenom,
                    "Obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtRegPrenom, errRegPrenom,
                    "Lettres uniquement (2-50 car.)");
            return false;
        }
        setOk(txtRegPrenom, errRegPrenom);
        return true;
    }

    private boolean validerRegEmail() {
        String v = txtRegEmail.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegEmail, errRegEmail,
                    "Obligatoire.");
            return false;
        }
        if (!v.matches(
                "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setErreur(txtRegEmail, errRegEmail,
                    "Format invalide.");
            return false;
        }
        setOk(txtRegEmail, errRegEmail);
        return true;
    }

    private boolean validerRegTel() {
        String v = txtRegTel.getText().trim();
        if (!v.isEmpty() &&
                !v.matches("^[+0-9][0-9\\s\\-\\.]{7,19}$")) {
            setErreur(txtRegTel, errRegTel,
                    "Format invalide. Ex: +216 22 123 456");
            return false;
        }
        setOk(txtRegTel, errRegTel);
        return true;
    }

    private boolean validerRegPassword() {
        String v = txtRegPassword.getText();
        if (v.isEmpty()) {
            setErreur(txtRegPassword, errRegPassword,
                    "Obligatoire.");
            return false;
        }
        if (v.length() < 6) {
            setErreur(txtRegPassword, errRegPassword,
                    "Minimum 6 caractères.");
            return false;
        }
        setOk(txtRegPassword, errRegPassword);
        return true;
    }

    private boolean validerRegConfirm() {
        String v1 = txtRegPassword.getText();
        String v2 = txtRegConfirm.getText();
        if (v2.isEmpty()) {
            setErreur(txtRegConfirm, errRegConfirm,
                    "Obligatoire.");
            return false;
        }
        if (!v1.equals(v2)) {
            setErreur(txtRegConfirm, errRegConfirm,
                    "Ne correspondent pas.");
            return false;
        }
        setOk(txtRegConfirm, errRegConfirm);
        return true;
    }

    private boolean validerRegRole() {
        if (cbRegRole.getValue() == null) {
            errRegRole.setText(
                    "⚠ Veuillez sélectionner un rôle.");
            return false;
        }
        errRegRole.setText("");
        return true;
    }

    private boolean validerCgu() {
        if (!chkCgu.isSelected()) {
            errCgu.setText(
                    "⚠ Vous devez accepter les conditions.");
            return false;
        }
        errCgu.setText("");
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private static final String STYLE_BASE =
            "-fx-background-color: #f8fafc;" +
                    "-fx-text-fill: #1e293b;" +
                    "-fx-prompt-text-fill: #94a3b8;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 11 14;" +
                    "-fx-font-size: 13;";

    private void setErreur(Control champ,
                           Label errLabel, String msg) {
        errLabel.setText("⚠ " + msg);
        champ.setStyle(STYLE_BASE +
                "-fx-border-color: #ef4444;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;");
    }

    private void setOk(Control champ, Label errLabel) {
        errLabel.setText("");
        champ.setStyle(STYLE_BASE +
                "-fx-border-color: #22c55e;" +
                "-fx-border-radius: 10;" +
                "-fx-border-width: 1.5;");
    }

    private void setErreurLabel(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
    }

    private void effacerErreursInscription() {
        txtRegNom.clear();      txtRegPrenom.clear();
        txtRegEmail.clear();    txtRegTel.clear();
        txtRegPassword.clear(); txtRegConfirm.clear();
        cbRegRole.setValue(null);
        chkCgu.setSelected(false);
        errRegNom.setText("");   errRegPrenom.setText("");
        errRegEmail.setText("");  errRegTel.setText("");
        errRegPassword.setText(""); errRegConfirm.setText("");
        errRegRole.setText("");  errCgu.setText("");
    }

    private int getRoleId(String roleNom) {
        List<String[]> roles = dao.findAllRoles();
        for (String[] role : roles)
            if (role[1].equals(roleNom))
                return Integer.parseInt(role[0]);
        return 1;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("EduNova");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}