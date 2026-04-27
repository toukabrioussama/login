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
    @FXML private TextField       txtNom;
    @FXML private TextField       txtPrenom;
    @FXML private TextField       txtEmail;
    @FXML private TextField       txtTelephone;
    @FXML private PasswordField   txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox        chkActif;
    @FXML private Button          btnSauvegarder;
    @FXML private ProgressIndicator progressIndicator;

    private final UserDAO    dao   = new UserDAO();
    private       User       userAModifier = null;
    private       List<String[]> roles;

    // Callback pour rafraîchir la liste après sauvegarde
    private Runnable onSauvegardeCallback;

    public void setOnSauvegardeCallback(Runnable callback) {
        this.onSauvegardeCallback = callback;
    }

    @FXML
    public void initialize() {
        roles = dao.findAllRoles();
        for (String[] role : roles) {
            cbRole.getItems().add(role[1]);
        }
    }

    // ── Mode AJOUT (par défaut) ───────────────────────────────────
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

        // Remplir le formulaire
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());
        txtTelephone.setText(u.getTelephone() != null ? u.getTelephone() : "");
        txtPassword.clear();
        chkActif.setSelected(u.isActif());
        cbRole.setValue(u.getRoleNom());
    }

    // ── Sauvegarder ───────────────────────────────────────────────
    @FXML
    private void handleSauvegarder() {
        if (!validerFormulaire()) return;

        // Afficher le chargement
        progressIndicator.setVisible(true);
        btnSauvegarder.setDisable(true);
        btnSauvegarder.setText("Enregistrement...");

        // Traitement en arrière-plan
        new Thread(() -> {
            boolean succes;

            if (userAModifier == null) {
                // AJOUT
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
                // MODIFICATION
                userAModifier.setNom(txtNom.getText().trim());
                userAModifier.setPrenom(txtPrenom.getText().trim());
                userAModifier.setEmail(txtEmail.getText().trim());
                userAModifier.setTelephone(txtTelephone.getText().trim());
                userAModifier.setPassword(txtPassword.getText());
                userAModifier.setActif(chkActif.isSelected());
                userAModifier.setRoleId(getRoleId(cbRole.getValue()));
                succes = dao.update(userAModifier);
            }

            boolean finalSucces = succes;
            Platform.runLater(() -> {
                progressIndicator.setVisible(false);
                btnSauvegarder.setDisable(false);
                btnSauvegarder.setText("Sauvegarder");

                if (finalSucces) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            userAModifier == null
                                    ? "Utilisateur ajouté avec succès !"
                                    : "Utilisateur modifié avec succès !");
                    if (onSauvegardeCallback != null) onSauvegardeCallback.run();
                    fermerFenetre();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Echec. L'email existe peut-être déjà.");
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
        for (String[] role : roles) {
            if (role[1].equals(roleNom)) return Integer.parseInt(role[0]);
        }
        return 1;
    }

    private boolean validerFormulaire() {
        if (txtNom.getText().trim().isEmpty()
                || txtPrenom.getText().trim().isEmpty()
                || txtEmail.getText().trim().isEmpty()
                || cbRole.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Nom, Prénom, Email et Rôle sont obligatoires.");
            return false;
        }
        if (!txtEmail.getText().contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide",
                    "Veuillez entrer un email valide.");
            return false;
        }
        if (userAModifier == null && txtPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe manquant",
                    "Le mot de passe est obligatoire pour un nouvel utilisateur.");
            return false;
        }
        if (!txtPassword.getText().isEmpty() && txtPassword.getText().length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe trop court",
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}