package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class UserFormController {

    @FXML private Label           lblTitreForm;
    @FXML private Label           lblMdpHint;

    // Champs
    @FXML private TextField       txtNom;
    @FXML private TextField       txtPrenom;
    @FXML private TextField       txtEmail;
    @FXML private TextField       txtTelephone;
    @FXML private PasswordField   txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox        chkActif;

    // Labels d'erreur sous chaque champ
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;
    @FXML private Label errTelephone;
    @FXML private Label errPassword;
    @FXML private Label errRole;

    // Boutons
    @FXML private Button            btnSauvegarder;
    @FXML private ProgressIndicator progressIndicator;

    private final UserDAO    dao   = new UserDAO();
    private       User       userAModifier = null;
    private       List<String[]> roles;
    private       Runnable   onSauvegardeCallback;

    public void setOnSauvegardeCallback(Runnable r) {
        this.onSauvegardeCallback = r;
    }

    // ── Initialisation ────────────────────────────────────────────
    @FXML
    public void initialize() {
        roles = dao.findAllRoles();
        for (String[] role : roles) cbRole.getItems().add(role[1]);

        // Validation en temps réel à la saisie
        txtNom.textProperty().addListener((obs, o, n) -> validerNom());
        txtPrenom.textProperty().addListener((obs, o, n) -> validerPrenom());
        txtEmail.textProperty().addListener((obs, o, n) -> validerEmail());
        txtTelephone.textProperty().addListener((obs, o, n) -> validerTelephone());
        txtPassword.textProperty().addListener((obs, o, n) -> validerPassword());
        cbRole.valueProperty().addListener((obs, o, n) -> validerRole());
    }

    // ── Mode AJOUT ────────────────────────────────────────────────
    public void configurerAjout() {
        lblTitreForm.setText("Ajouter un utilisateur");
        lblMdpHint.setText("Mot de passe *");
        userAModifier = null;
    }

    // ── Mode MODIFICATION ─────────────────────────────────────────
    public void configurerModification(User u) {
        userAModifier = u;
        lblTitreForm.setText("Modifier l'utilisateur");
        lblMdpHint.setText("Nouveau mot de passe (vide = inchangé)");
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());
        txtTelephone.setText(u.getTelephone() != null ? u.getTelephone() : "");
        txtPassword.clear();
        chkActif.setSelected(u.isActif());
        cbRole.setValue(u.getRoleNom());
        effacerErreurs();
    }

    // ══════════════════════════════════════════════════════════════
    //  VALIDATIONS INDIVIDUELLES
    // ══════════════════════════════════════════════════════════════

    private boolean validerNom() {
        String v = txtNom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtNom, errNom, "Le nom est obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtNom, errNom,
                    "Lettres uniquement, 2 à 50 caractères.");
            return false;
        }
        setOk(txtNom, errNom);
        return true;
    }

    private boolean validerPrenom() {
        String v = txtPrenom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtPrenom, errPrenom, "Le prénom est obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtPrenom, errPrenom,
                    "Lettres uniquement, 2 à 50 caractères.");
            return false;
        }
        setOk(txtPrenom, errPrenom);
        return true;
    }

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

    private boolean validerTelephone() {
        String v = txtTelephone.getText().trim();
        // Optionnel mais si rempli → doit être valide
        if (!v.isEmpty() && !v.matches("^[+0-9][0-9\\s\\-\\.]{7,19}$")) {
            setErreur(txtTelephone, errTelephone,
                    "Format invalide. Ex: +216 22 123 456");
            return false;
        }
        setOk(txtTelephone, errTelephone);
        return true;
    }

    private boolean validerPassword() {
        String v = txtPassword.getText();
        // Obligatoire seulement à l'ajout
        if (userAModifier == null && v.isEmpty()) {
            setErreur(txtPassword, errPassword,
                    "Le mot de passe est obligatoire.");
            return false;
        }
        if (!v.isEmpty() && v.length() < 6) {
            setErreur(txtPassword, errPassword,
                    "Minimum 6 caractères requis.");
            return false;
        }
        if (!v.isEmpty() && v.length() > 50) {
            setErreur(txtPassword, errPassword,
                    "Maximum 50 caractères.");
            return false;
        }
        setOk(txtPassword, errPassword);
        return true;
    }

    private boolean validerRole() {
        if (cbRole.getValue() == null) {
            errRole.setText("Veuillez sélectionner un rôle.");
            return false;
        }
        errRole.setText("");
        return true;
    }

    // ── Valider tout d'un coup ────────────────────────────────────
    private boolean validerTout() {
        boolean n  = validerNom();
        boolean p  = validerPrenom();
        boolean e  = validerEmail();
        boolean t  = validerTelephone();
        boolean pw = validerPassword();
        boolean r  = validerRole();
        return n && p && e && t && pw && r;
    }

    // ══════════════════════════════════════════════════════════════
    //  STYLE CHAMPS : ERREUR / OK
    // ══════════════════════════════════════════════════════════════

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

    private void effacerErreurs() {
        errNom.setText("");      errPrenom.setText("");
        errEmail.setText("");    errTelephone.setText("");
        errPassword.setText(""); errRole.setText("");
    }

    // ── Sauvegarder ───────────────────────────────────────────────
    @FXML
    private void handleSauvegarder() {
        if (!validerTout()) return;

        progressIndicator.setVisible(true);
        btnSauvegarder.setDisable(true);
        btnSauvegarder.setText("Enregistrement...");

        new Thread(() -> {
            boolean succes;
            if (userAModifier == null) {
                User u = new User();
                u.setNom(txtNom.getText().trim());
                u.setPrenom(txtPrenom.getText().trim());
                u.setEmail(txtEmail.getText().trim());
                u.setTelephone(txtTelephone.getText().trim());
                u.setPassword(txtPassword.getText());
                u.setActif(chkActif.isSelected());
                u.setRoleId(getRoleId(cbRole.getValue()));
                succes = dao.insert(u);
            } else {
                userAModifier.setNom(txtNom.getText().trim());
                userAModifier.setPrenom(txtPrenom.getText().trim());
                userAModifier.setEmail(txtEmail.getText().trim());
                userAModifier.setTelephone(txtTelephone.getText().trim());
                userAModifier.setPassword(txtPassword.getText());
                userAModifier.setActif(chkActif.isSelected());
                userAModifier.setRoleId(getRoleId(cbRole.getValue()));
                succes = dao.update(userAModifier);
            }

            boolean ok = succes;
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                btnSauvegarder.setDisable(false);
                btnSauvegarder.setText("Sauvegarder");

                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            userAModifier == null
                                    ? "Utilisateur ajouté avec succès !"
                                    : "Utilisateur modifié avec succès !");
                    if (onSauvegardeCallback != null)
                        onSauvegardeCallback.run();
                    fermerFenetre();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Échec. L'email existe peut-être déjà.");
                }
            });
        }).start();
    }

    @FXML
    private void handleAnnuler() { fermerFenetre(); }

    private void fermerFenetre() {
        ((Stage) txtNom.getScene().getWindow()).close();
    }

    private int getRoleId(String roleNom) {
        for (String[] role : roles)
            if (role[1].equals(roleNom)) return Integer.parseInt(role[0]);
        return 1;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}