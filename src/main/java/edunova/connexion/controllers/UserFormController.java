package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class UserFormController {

    @FXML private Label            lblTitreForm;
    @FXML private Label            lblMdpHint;
    @FXML private TextField        txtNom;
    @FXML private TextField        txtPrenom;
    @FXML private TextField        txtEmail;
    @FXML private TextField        txtTelephone;
    @FXML private PasswordField    txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox         chkActif;
    @FXML private Label            errNom;
    @FXML private Label            errPrenom;
    @FXML private Label            errEmail;
    @FXML private Label            errPassword;
    @FXML private Label            errRole;
    @FXML private Button           btnSauvegarder;
    @FXML private ProgressIndicator progressIndicator;

    // ── Dropdown téléphone dark ───────────────────────────────────
    @FXML private Button    btnPaysForm;
    @FXML private VBox      dropdownPaysForm;
    @FXML private TextField txtRecherchePaysForm;
    @FXML private VBox      listePaysForm;
    private boolean dropdownFormVisible  = false;
    private String  codePaysCourantForm  = "+216";

    private final UserDAO    dao   = new UserDAO();
    private       User       userAModifier = null;
    private       List<String[]> roles;
    private       Runnable   onSauvegardeCallback;

    public void setOnSauvegardeCallback(Runnable r) {
        this.onSauvegardeCallback = r;
    }

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        roles = dao.findAllRoles();
        for (String[] role : roles)
            cbRole.getItems().add(role[1]);

        // Validations temps réel
        txtNom.textProperty().addListener(
                (o, old, n) -> { if (!n.isEmpty()) validerNom(); });
        txtPrenom.textProperty().addListener(
                (o, old, n) -> { if (!n.isEmpty()) validerPrenom(); });
        txtEmail.textProperty().addListener(
                (o, old, n) -> { if (!n.isEmpty()) validerEmail(); });
        txtPassword.textProperty().addListener(
                (o, old, n) -> { if (!n.isEmpty()) validerPassword(); });
        cbRole.valueProperty().addListener(
                (o, old, n) -> validerRole());

        // Recherche pays en temps réel
        txtRecherchePaysForm.textProperty()
                .addListener((obs, old, n) ->
                        filtrerPaysForm(n));
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
        lblMdpHint.setText(
                "Nouveau mot de passe (vide = inchangé)");
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());

        // Pré-remplir le téléphone
        if (u.getTelephone() != null &&
                !u.getTelephone().isEmpty()) {
            String tel = u.getTelephone();
            // Extraire le code si présent
            if (tel.startsWith("+")) {
                String[] parts = tel.split(" ", 2);
                codePaysCourantForm = parts[0];
                // Trouver le drapeau
                String drapeau =
                        PhonePickerController.PAYS
                                .entrySet().stream()
                                .filter(e -> e.getValue()
                                        .equals(parts[0]))
                                .map(e -> e.getKey().split(" ")[0])
                                .findFirst().orElse("🌍");
                btnPaysForm.setText(
                        drapeau + " " + parts[0]);
                txtTelephone.setText(
                        parts.length > 1 ? parts[1] : "");
            } else {
                txtTelephone.setText(tel);
            }
        }

        txtPassword.clear();
        chkActif.setSelected(u.isActif());
        cbRole.setValue(u.getRoleNom());
        effacerErreurs();
    }

    // ══════════════════════════════════════════════════════════════
    //  DROPDOWN TÉLÉPHONE DARK
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleToggleDropdownForm() {
        dropdownFormVisible = !dropdownFormVisible;
        dropdownPaysForm.setVisible(dropdownFormVisible);
        dropdownPaysForm.setManaged(dropdownFormVisible);
        if (dropdownFormVisible) {
            remplirListePaysForm();
            txtRecherchePaysForm.clear();
            txtRecherchePaysForm.requestFocus();
        }
    }

    private void remplirListePaysForm() {
        listePaysForm.getChildren().clear();
        PhonePickerController.PAYS.forEach((pays, code) ->
                listePaysForm.getChildren().add(
                        PhonePickerController.creerItem(
                                pays, code, true,
                                () -> selectionnerPaysForm(pays, code))));
    }

    private void filtrerPaysForm(String keyword) {
        listePaysForm.getChildren().clear();
        PhonePickerController.PAYS.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase()
                        .contains(keyword.toLowerCase()) ||
                        e.getValue().contains(keyword))
                .forEach(e -> listePaysForm.getChildren().add(
                        PhonePickerController.creerItem(
                                e.getKey(), e.getValue(), true,
                                () -> selectionnerPaysForm(
                                        e.getKey(), e.getValue()))));
    }

    private void selectionnerPaysForm(String pays, String code) {
        String emoji = PhonePickerController.getEmoji(pays);
        btnPaysForm.setText(emoji + "  " + code);
        codePaysCourantForm = code;

        String numero = txtTelephone.getText()
                .replaceAll("^\\+\\d+\\s*", "").trim();
        txtTelephone.setText(numero);

        dropdownFormVisible = false;
        dropdownPaysForm.setVisible(false);
        dropdownPaysForm.setManaged(false);
        txtTelephone.requestFocus();
    }

    // ══════════════════════════════════════════════════════════════
    //  SAUVEGARDER
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleSauvegarder() {
        if (!validerTout()) return;

        progressIndicator.setVisible(true);
        btnSauvegarder.setDisable(true);
        btnSauvegarder.setText("Enregistrement...");

        new Thread(() -> {
            boolean succes;

            // Construire le numéro complet
            String numero = txtTelephone.getText().trim();
            if (!numero.isEmpty() &&
                    !numero.startsWith("+")) {
                numero = codePaysCourantForm + " " + numero;
            }

            if (userAModifier == null) {
                User u = new User();
                u.setNom(txtNom.getText().trim());
                u.setPrenom(txtPrenom.getText().trim());
                u.setEmail(txtEmail.getText().trim());
                u.setTelephone(numero);
                u.setPassword(txtPassword.getText());
                u.setActif(chkActif.isSelected());
                u.setRoleId(getRoleId(cbRole.getValue()));
                succes = dao.insert(u);
            } else {
                userAModifier.setNom(
                        txtNom.getText().trim());
                userAModifier.setPrenom(
                        txtPrenom.getText().trim());
                userAModifier.setEmail(
                        txtEmail.getText().trim());
                userAModifier.setTelephone(numero);
                userAModifier.setPassword(
                        txtPassword.getText());
                userAModifier.setActif(
                        chkActif.isSelected());
                userAModifier.setRoleId(
                        getRoleId(cbRole.getValue()));
                succes = dao.update(userAModifier);
            }

            boolean ok = succes;
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                btnSauvegarder.setDisable(false);
                btnSauvegarder.setText("Sauvegarder");

                if (ok) {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Succès",
                            userAModifier == null
                                    ? "Utilisateur ajouté !"
                                    : "Utilisateur modifié !");
                    if (onSauvegardeCallback != null)
                        onSauvegardeCallback.run();
                    fermerFenetre();
                } else {
                    showAlert(Alert.AlertType.ERROR,
                            "Erreur",
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

    // ══════════════════════════════════════════════════════════════
    //  VALIDATIONS
    // ══════════════════════════════════════════════════════════════

    private boolean validerNom() {
        String v = txtNom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtNom, errNom, "Obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtNom, errNom,
                    "Lettres uniquement (2-50 car.)");
            return false;
        }
        setOk(txtNom, errNom);
        return true;
    }

    private boolean validerPrenom() {
        String v = txtPrenom.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtPrenom, errPrenom, "Obligatoire.");
            return false;
        }
        if (!v.matches("[a-zA-ZÀ-ÿ\\s\\-']{2,50}")) {
            setErreur(txtPrenom, errPrenom,
                    "Lettres uniquement (2-50 car.)");
            return false;
        }
        setOk(txtPrenom, errPrenom);
        return true;
    }

    private boolean validerEmail() {
        String v = txtEmail.getText().trim();
        if (v.isEmpty()) {
            setErreur(txtEmail, errEmail, "Obligatoire.");
            return false;
        }
        if (!v.matches(
                "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            setErreur(txtEmail, errEmail, "Format invalide.");
            return false;
        }
        setOk(txtEmail, errEmail);
        return true;
    }

    private boolean validerPassword() {
        String v = txtPassword.getText();
        if (userAModifier == null && v.isEmpty()) {
            setErreur(txtPassword, errPassword,
                    "Obligatoire.");
            return false;
        }
        if (!v.isEmpty() && v.length() < 6) {
            setErreur(txtPassword, errPassword,
                    "Minimum 6 caractères.");
            return false;
        }
        setOk(txtPassword, errPassword);
        return true;
    }

    private boolean validerRole() {
        if (cbRole.getValue() == null) {
            errRole.setText("⚠ Obligatoire.");
            return false;
        }
        errRole.setText("");
        return true;
    }

    private boolean validerTout() {
        boolean n  = validerNom();
        boolean p  = validerPrenom();
        boolean e  = validerEmail();
        boolean pw = validerPassword();
        boolean r  = validerRole();
        return n && p && e && pw && r;
    }

    // ── Style champs ──────────────────────────────────────────────
    private static final String STYLE_BASE_DARK =
            "-fx-background-color: #0f0f1a;" +
                    "-fx-text-fill: #e2e8f0;" +
                    "-fx-prompt-text-fill: #475569;" +
                    "-fx-background-radius: 8;" +
                    "-fx-padding: 10 14;";

    private void setErreur(Control c,
                           Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
        c.setStyle(STYLE_BASE_DARK +
                "-fx-border-color: #f87171;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1.5;");
    }

    private void setOk(Control c, Label lbl) {
        lbl.setText("");
        c.setStyle(STYLE_BASE_DARK +
                "-fx-border-color: #22c55e;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1.5;");
    }

    private void effacerErreurs() {
        errNom.setText("");
        errPrenom.setText("");
        errEmail.setText("");
        errPassword.setText("");
        errRole.setText("");
    }

    private int getRoleId(String roleNom) {
        for (String[] role : roles)
            if (role[1].equals(roleNom))
                return Integer.parseInt(role[0]);
        return 1;
    }

    private void showAlert(Alert.AlertType type,
                           String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}