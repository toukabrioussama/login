package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class UserController {

    // ── Tableau ───────────────────────────────────────────────────
    @FXML private TableView<User>            tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colTel;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Boolean> colActif;

    // ── Formulaire ────────────────────────────────────────────────
    @FXML private TextField     txtRecherche;
    @FXML private TextField     txtNom;
    @FXML private TextField     txtPrenom;
    @FXML private TextField     txtEmail;
    @FXML private TextField     txtTelephone;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private CheckBox      chkActif;

    private final UserDAO    dao  = new UserDAO();
    private       User       userSelectionne = null;
    private       List<String[]> roles;

    // ── Initialisation ────────────────────────────────────────────
    @FXML
    public void initialize() {

        // Lier colonnes aux propriétés
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleNom"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));

        // Colonne Actif : afficher OUI / NON avec couleur
        colActif.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    return;
                }
                if (val) {
                    setText("OUI");
                    setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                } else {
                    setText("NON");
                    setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                }
            }
        });

        // Charger les rôles dans le ComboBox
        roles = dao.findAllRoles();
        for (String[] role : roles) {
            cbRole.getItems().add(role[1]);
        }

        // Clic sur une ligne → remplir formulaire
        tableUsers.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null) {
                        remplirFormulaire(selected);
                    }
                });

        chargerTout();
    }

    // ── Charger tous les utilisateurs ────────────────────────────
    private void chargerTout() {
        ObservableList<User> data =
                FXCollections.observableArrayList(dao.findAll());
        tableUsers.setItems(data);
    }

    // ── Remplir le formulaire au clic ────────────────────────────
    private void remplirFormulaire(User u) {
        userSelectionne = u;
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());
        txtTelephone.setText(u.getTelephone() != null ? u.getTelephone() : "");
        txtPassword.clear();
        chkActif.setSelected(u.isActif());
        cbRole.setValue(u.getRoleNom());
    }

    // ── AJOUTER ──────────────────────────────────────────────────
    @FXML
    private void handleAjouter() {
        if (!validerFormulaire(true)) return;

        User u = new User();
        u.setNom(txtNom.getText().trim());
        u.setPrenom(txtPrenom.getText().trim());
        u.setEmail(txtEmail.getText().trim());
        u.setTelephone(txtTelephone.getText().trim());
        u.setPassword(txtPassword.getText());
        u.setActif(chkActif.isSelected());
        u.setRoleId(getRoleId(cbRole.getValue()));

        if (dao.insert(u)) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Succès", "Utilisateur ajouté avec succès !");
            viderFormulaire();
            chargerTout();
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "Échec de l'ajout.\nL'email existe peut-être déjà.");
        }
    }

    // ── MODIFIER ─────────────────────────────────────────────────
    @FXML
    private void handleModifier() {
        if (userSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Sélectionnez un utilisateur dans le tableau.");
            return;
        }
        if (!validerFormulaire(false)) return;

        userSelectionne.setNom(txtNom.getText().trim());
        userSelectionne.setPrenom(txtPrenom.getText().trim());
        userSelectionne.setEmail(txtEmail.getText().trim());
        userSelectionne.setTelephone(txtTelephone.getText().trim());
        userSelectionne.setPassword(txtPassword.getText());
        userSelectionne.setActif(chkActif.isSelected());
        userSelectionne.setRoleId(getRoleId(cbRole.getValue()));

        if (dao.update(userSelectionne)) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Succès", "Utilisateur modifié avec succès !");
            viderFormulaire();
            chargerTout();
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "Échec de la modification.");
        }
    }

    // ── SUPPRIMER ────────────────────────────────────────────────
    @FXML
    private void handleSupprimer() {
        if (userSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Attention",
                    "Sélectionnez un utilisateur dans le tableau.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer " + userSelectionne.getPrenom()
                + " " + userSelectionne.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (dao.delete(userSelectionne.getId())) {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Succès", "Utilisateur supprimé !");
                    viderFormulaire();
                    chargerTout();
                } else {
                    showAlert(Alert.AlertType.ERROR,
                            "Erreur", "Échec de la suppression.");
                }
            }
        });
    }

    // ── RECHERCHER ───────────────────────────────────────────────
    @FXML
    private void handleRecherche() {
        String keyword = txtRecherche.getText().trim();
        if (keyword.isEmpty()) {
            chargerTout();
            return;
        }
        ObservableList<User> data =
                FXCollections.observableArrayList(dao.search(keyword));
        tableUsers.setItems(data);
        if (data.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION,
                    "Recherche", "Aucun résultat pour : " + keyword);
        }
    }

    // ── ACTUALISER ───────────────────────────────────────────────
    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerTout();
    }

    // ── VIDER formulaire ─────────────────────────────────────────
    @FXML
    private void handleVider() {
        viderFormulaire();
    }

    private void viderFormulaire() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtPassword.clear();
        cbRole.setValue(null);
        chkActif.setSelected(true);
        userSelectionne = null;
        tableUsers.getSelectionModel().clearSelection();
    }

    // ── Récupérer id du rôle sélectionné ─────────────────────────
    private int getRoleId(String roleNom) {
        for (String[] role : roles) {
            if (role[1].equals(roleNom)) {
                return Integer.parseInt(role[0]);
            }
        }
        return 1;
    }

    // ── Validation ───────────────────────────────────────────────
    private boolean validerFormulaire(boolean isAjout) {
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
        if (isAjout && txtPassword.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Mot de passe manquant",
                    "Le mot de passe est obligatoire pour un nouvel utilisateur.");
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