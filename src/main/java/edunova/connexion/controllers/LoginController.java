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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class LoginController {

    // ── Connexion ─────────────────────────────────────────────────
    @FXML private VBox          panneauConnexion;
    @FXML private TextField     txtEmailO;
    @FXML private PasswordField txtPasswordO;
    @FXML private Label         errLoginEmail;
    @FXML private Label         errLoginPassword;

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

    // ── Dropdown téléphone ────────────────────────────────────────
    @FXML private Button    btnPaysReg;
    @FXML private VBox      dropdownPaysReg;
    @FXML private TextField txtRecherchePaysReg;
    @FXML private VBox      listePaysReg;
    private boolean dropdownRegVisible = false;
    private String  codePaysCourantReg = "+216";

    private final UserDAO dao = new UserDAO();

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        cbRegRole.getItems().addAll(
                "Administrateur", "Enseignant", "Etudiant");

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
        txtRegPassword.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegPassword(); });
        txtRegConfirm.textProperty().addListener((o, old, n) -> {
            if (!n.isEmpty()) validerRegConfirm(); });

        // Recherche pays en temps réel
        txtRecherchePaysReg.textProperty()
                .addListener((obs, old, n) ->
                        filtrerPays(n, listePaysReg,
                                true, btnPaysReg));
    }

    // ══════════════════════════════════════════════════════════════
    //  DROPDOWN TÉLÉPHONE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleToggleDropdownReg() {
        dropdownRegVisible = !dropdownRegVisible;
        dropdownPaysReg.setVisible(dropdownRegVisible);
        dropdownPaysReg.setManaged(dropdownRegVisible);
        if (dropdownRegVisible) {
            remplirListePays(listePaysReg, true, btnPaysReg);
            txtRecherchePaysReg.clear();
            txtRecherchePaysReg.requestFocus();
        }
    }

    private void remplirListePays(VBox liste,
                                  boolean light, Button btnPays) {
        liste.getChildren().clear();
        PhonePickerController.PAYS.forEach((pays, code) ->
                liste.getChildren().add(
                        PhonePickerController.creerItem(
                                pays, code, !light,
                                () -> selectionnerPays(
                                        pays, code, btnPays))));
    }

    private void filtrerPays(String keyword, VBox liste,
                             boolean light, Button btnPays) {
        liste.getChildren().clear();
        PhonePickerController.PAYS.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase()
                        .contains(keyword.toLowerCase()) ||
                        e.getValue().contains(keyword))
                .forEach(e -> liste.getChildren().add(
                        PhonePickerController.creerItem(
                                e.getKey(), e.getValue(), !light,
                                () -> selectionnerPays(
                                        e.getKey(), e.getValue(), btnPays))));
    }

    private void selectionnerPays(String pays, String code,
                                  Button btnPays) {
        // Bouton : emoji + code
        String emoji = PhonePickerController.getEmoji(pays);
        btnPays.setText(emoji + "  " + code);
        codePaysCourantReg = code;

        // Nettoyer le numéro existant
        String numero = txtRegTel.getText()
                .replaceAll("^\\+\\d+\\s*", "").trim();
        txtRegTel.setText(numero);

        // Fermer dropdown
        dropdownRegVisible = false;
        dropdownPaysReg.setVisible(false);
        dropdownPaysReg.setManaged(false);
        txtRegTel.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════
    //  CONNEXION
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleLogin() {
        boolean e = validerLoginEmail();
        boolean p = validerLoginPassword();
        if (!e || !p) return;
        effectuerConnexion();
    }

    private void effectuerConnexion() {
        String email    = txtEmailO.getText().trim();
        String password = txtPasswordO.getText();

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT u.id_u, u.password_u, " +
                            "       u.nom_u, u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND u.actif_u = 1";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (PasswordUtils.verify(password,
                        rs.getString("password_u"))) {

                    String role = rs.getString("nom_r");
                    SessionManager s =
                            SessionManager.getInstance();
                    s.setUserId(rs.getInt("id_u"));
                    s.setEmail(email);
                    s.setRole(role);

                    enregistrerHistorique(conn,
                            rs.getInt("id_u"), true);
                    ouvrirDashboard();

                } else {
                    enregistrerHistorique(conn,
                            rs.getInt("id_u"), false);
                    setErreur(txtPasswordO,
                            errLoginPassword,
                            "Mot de passe incorrect.");
                }
            } else {
                setErreur(txtEmailO, errLoginEmail,
                        "Aucun compte actif trouvé.");
            }

        } catch (SQLException ex) {
            showAlert("Erreur BD : " + ex.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur : " + ex.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  GOOGLE
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleGoogleLogin() {
        showAlert("🔄 Ouverture du navigateur Google...\n\n" +
                "Une fenêtre va s'ouvrir dans votre navigateur.");

        new Thread(() -> {
            try {
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

        try (Connection conn =
                     DatabaseConnection.getConnection()) {

            String sql =
                    "SELECT u.id_u, u.nom_u, " +
                            "       u.prenom_u, r.nom_r " +
                            "FROM user u " +
                            "JOIN role r ON u.role_id = r.id_r " +
                            "WHERE u.email_u = ? AND u.actif_u = 1";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("nom_r");
                SessionManager s =
                        SessionManager.getInstance();
                s.setUserId(rs.getInt("id_u"));
                s.setEmail(email);
                s.setRole(role);
                enregistrerHistorique(conn,
                        rs.getInt("id_u"), true);
                showAlert("✅ Connexion Google réussie !\n" +
                        "Bienvenue " +
                        rs.getString("prenom_u") + " " +
                        rs.getString("nom_u"));
                ouvrirDashboard();
            } else {
                showAlert("📋 Compte Google non trouvé.\n\n" +
                        "Veuillez compléter votre inscription.");
                panneauConnexion.setVisible(false);
                panneauConnexion.setManaged(false);
                panneauInscription.setVisible(true);
                panneauInscription.setManaged(true);
                txtRegNom.setText(nom);
                txtRegPrenom.setText(prenom);
                txtRegEmail.setText(email);
                String mdpTemp = UUID.randomUUID()
                        .toString().substring(0, 8);
                txtRegPassword.setText(mdpTemp);
                txtRegConfirm.setText(mdpTemp);
                showAlert("🔑 Mot de passe temporaire :\n" +
                        mdpTemp);
            }
        } catch (SQLException ex) {
            showAlert("Erreur BD : " + ex.getMessage());
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
            showAlert("Erreur : " + ex.getMessage());
        }
    }

    // ── Historique ────────────────────────────────────────────────
    private void enregistrerHistorique(
            Connection conn, int userId, boolean succes) {
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

    // ── Mot de passe oublié ───────────────────────────────────────
    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/views/forgot_password.fxml"));
            Stage stage = new Stage();
            stage.setTitle(
                    "EduNova - Mot de passe oublié");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initOwner(
                    txtEmailO.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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
    }

    @FXML
    private void handleInscription() {
        boolean n  = validerRegNom();
        boolean p  = validerRegPrenom();
        boolean e  = validerRegEmail();
        boolean pw = validerRegPassword();
        boolean cf = validerRegConfirm();
        boolean r  = validerRegRole();
        boolean cg = validerCgu();
        if (!n || !p || !e || !pw || !cf || !r || !cg)
            return;

        User u = new User();
        u.setNom(txtRegNom.getText().trim());
        u.setPrenom(txtRegPrenom.getText().trim());
        u.setEmail(txtRegEmail.getText().trim());
        String numero = txtRegTel.getText().trim();
        if (!numero.isEmpty() && !numero.startsWith("+"))
            numero = codePaysCourantReg + " " + numero;
        u.setTelephone(numero);
        u.setPassword(txtRegPassword.getText());
        u.setActif(true);
        u.setRoleId(getRoleId(cbRegRole.getValue()));

        if (dao.insert(u)) {
            showAlert("✅ Compte créé avec succès !\n\n" +
                    "Bienvenue " + u.getPrenom() +
                    " " + u.getNom() + " !");
            handleShowConnexion();
        } else {
            setErreurLabel(errRegEmail,
                    "Cet email est déjà utilisé.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  VALIDATIONS
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

    private boolean validerRegNom() {
        String v = txtRegNom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtRegNom, errRegNom, "Obligatoire.");
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
        txtRegNom.clear();
        txtRegPrenom.clear();
        txtRegEmail.clear();
        txtRegTel.clear();
        txtRegPassword.clear();
        txtRegConfirm.clear();
        cbRegRole.setValue(null);
        chkCgu.setSelected(false);
        errRegNom.setText("");
        errRegPrenom.setText("");
        errRegEmail.setText("");
        errRegTel.setText("");
        errRegPassword.setText("");
        errRegConfirm.setText("");
        errRegRole.setText("");
        errCgu.setText("");
        dropdownRegVisible = false;
        dropdownPaysReg.setVisible(false);
        dropdownPaysReg.setManaged(false);
        btnPaysReg.setText("🇹🇳 +216");
        codePaysCourantReg = "+216";
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