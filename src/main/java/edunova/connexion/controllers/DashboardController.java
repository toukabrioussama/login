package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;
import edunova.connexion.tools.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // ── NAVBAR ────────────────────────────────────────────────────
    @FXML private Label lblPageTitre;
    @FXML private Label lblDateHeure;
    @FXML private Label lblUserNom;
    @FXML private Label lblUserRole;
    @FXML private Label lblAvatarInitiales;

    // ── PAGES ─────────────────────────────────────────────────────
    @FXML private javafx.scene.control.ScrollPane pageDashboard;
    @FXML private VBox pageUsers;

    // ── STATS DASHBOARD ───────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalAdmins;
    @FXML private Label lblTotalEnseignants;
    @FXML private Label lblTotalEtudiants;
    @FXML private Label lblEvolutionUsers;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;

    // ── TABLE DASHBOARD ───────────────────────────────────────────
    @FXML private TableView<User>           tableDerniers;
    @FXML private TableColumn<User, String> dColNom;
    @FXML private TableColumn<User, String> dColEmail;
    @FXML private TableColumn<User, String> dColRole;
    @FXML private TableColumn<User, Boolean> dColActif;

    // ── TABLE USERS ───────────────────────────────────────────────
    @FXML private TableView<User>            tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colNom;
    @FXML private TableColumn<User, String>  colPrenom;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colTel;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Boolean> colActif;
    @FXML private TableColumn<User, Void>    colActions;

    // ── RECHERCHE ─────────────────────────────────────────────────
    @FXML private TextField txtRecherche;
    @FXML private TextField txtRechercheGlobal;

    // ── BOUTONS MENU ──────────────────────────────────────────────
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuUsers;
    @FXML private Button btnMenuEtudiants;
    @FXML private Button btnMenuEnseignants;
    @FXML private Button btnMenuClasses;

    private final UserDAO dao = new UserDAO();

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        configurerSession();
        configurerDate();
        configurerTableDashboard();
        configurerTableUsers();
        chargerStatistiques();
        chargerTousUsers();
    }

    // ── Session utilisateur ───────────────────────────────────────
    private void configurerSession() {
        SessionManager s = SessionManager.getInstance();
        lblUserNom.setText(s.getEmail());
        lblUserRole.setText(s.getRole());
        String initiale = s.getEmail().substring(0, 1).toUpperCase();
        lblAvatarInitiales.setText(initiale);
    }

    // ── Date heure ────────────────────────────────────────────────
    private void configurerDate() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        lblDateHeure.setText(date);
    }

    // ── Table dashboard (derniers users) ─────────────────────────
    private void configurerTableDashboard() {
        dColNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        dColRole.setCellValueFactory(new PropertyValueFactory<>("roleNom"));
        dColActif.setCellValueFactory(new PropertyValueFactory<>("actif"));

        dColActif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); return; }
                setText(val ? "Actif" : "Inactif");
                setStyle(val
                        ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                        : "-fx-text-fill: #f87171; -fx-font-weight: bold;");
            }
        });
    }

    // ── Table utilisateurs avec boutons Modifier/Supprimer ────────
    private void configurerTableUsers() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("roleNom"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));

        // Colonne statut
        colActif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); return; }
                setText(val ? "Actif" : "Inactif");
                setStyle(val
                        ? "-fx-text-fill: #22c55e; -fx-font-weight: bold;"
                        : "-fx-text-fill: #f87171; -fx-font-weight: bold;");
            }
        });

        // Colonne Actions : boutons Modifier / Supprimer
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Modifier");
            private final Button btnDel  = new Button("Supprimer");
            private final HBox   box     = new HBox(6, btnEdit, btnDel);

            {
                btnEdit.setStyle(
                        "-fx-background-color: #2d1b69; -fx-text-fill: #a78bfa;" +
                                "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand;");
                btnDel.setStyle(
                        "-fx-background-color: #2d1b1b; -fx-text-fill: #f87171;" +
                                "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand;");

                btnEdit.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    ouvrirFormModification(u);
                });
                btnDel.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    supprimerUser(u);
                });
            }

            @Override
            protected void updateItem(Void val, boolean empty) {
                super.updateItem(val, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Charger statistiques ──────────────────────────────────────
    private void chargerStatistiques() {
        List<User> tous = dao.findAll();

        long total      = tous.size();
        long admins     = tous.stream().filter(u -> "Administrateur".equals(u.getRoleNom())).count();
        long enseignants= tous.stream().filter(u -> "Enseignant".equals(u.getRoleNom())).count();
        long etudiants  = tous.stream().filter(u -> "Etudiant".equals(u.getRoleNom())).count();
        long actifs     = tous.stream().filter(User::isActif).count();
        long inactifs   = total - actifs;

        lblTotalUsers.setText(String.valueOf(total));
        lblTotalAdmins.setText(String.valueOf(admins));
        lblTotalEnseignants.setText(String.valueOf(enseignants));
        lblTotalEtudiants.setText(String.valueOf(etudiants));
        lblEvolutionUsers.setText(actifs + " comptes actifs");
        lblActifs.setText(String.valueOf(actifs));
        lblInactifs.setText(String.valueOf(inactifs));

        // Derniers 5 users ajoutés
        List<User> derniers = tous.stream()
                .limit(5).toList();
        tableDerniers.setItems(FXCollections.observableArrayList(derniers));
    }

    // ── Charger tous les utilisateurs ────────────────────────────
    private void chargerTousUsers() {
        ObservableList<User> data =
                FXCollections.observableArrayList(dao.findAll());
        tableUsers.setItems(data);
    }

    // ── Navigation sidebar ────────────────────────────────────────
    @FXML
    private void handleMenuDashboard() {
        afficherPage("dashboard");
    }

    @FXML
    private void handleMenuUsers() {
        afficherPage("users");
    }

    @FXML
    private void handleMenuEtudiants() {
        showAlert("Info", "Module Etudiants — bientôt disponible !");
    }

    @FXML
    private void handleMenuEnseignants() {
        showAlert("Info", "Module Enseignants — bientôt disponible !");
    }

    @FXML
    private void handleMenuClasses() {
        showAlert("Info", "Module Classes — bientôt disponible !");
    }

    private void afficherPage(String page) {
        // Masquer toutes les pages
        pageDashboard.setVisible(false);
        pageDashboard.setManaged(false);
        pageUsers.setVisible(false);
        pageUsers.setManaged(false);

        // Réinitialiser styles sidebar
        String styleActif = "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                "-fx-font-size: 13; -fx-background-radius: 8;" +
                "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";
        String styleNormal= "-fx-background-color: transparent; -fx-text-fill: #94a3b8;" +
                "-fx-font-size: 13; -fx-background-radius: 8;" +
                "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";

        btnMenuDashboard.setStyle(styleNormal);
        btnMenuUsers.setStyle(styleNormal);

        switch (page) {
            case "dashboard" -> {
                pageDashboard.setVisible(true);
                pageDashboard.setManaged(true);
                lblPageTitre.setText("Tableau de bord");
                btnMenuDashboard.setStyle(styleActif);
                chargerStatistiques();
            }
            case "users" -> {
                pageUsers.setVisible(true);
                pageUsers.setManaged(true);
                lblPageTitre.setText("Gestion des Utilisateurs");
                btnMenuUsers.setStyle(styleActif);
                chargerTousUsers();
            }
        }
    }

    // ── Ouvrir formulaire AJOUT ───────────────────────────────────
    @FXML
    private void handleAjouterUser() {
        ouvrirFormulaire(null);
    }

    // ── Ouvrir formulaire MODIFICATION ───────────────────────────
    private void ouvrirFormModification(User u) {
        ouvrirFormulaire(u);
    }

    private void ouvrirFormulaire(User userAModifier) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/user_form.fxml"));
            Stage stage = new Stage();
            stage.setTitle(userAModifier == null
                    ? "Ajouter un utilisateur"
                    : "Modifier l'utilisateur");
            stage.setScene(new Scene(loader.load()));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            UserFormController ctrl = loader.getController();
            if (userAModifier == null) {
                ctrl.configurerAjout();
            } else {
                ctrl.configurerModification(userAModifier);
            }
            // Rafraîchir après sauvegarde
            ctrl.setOnSauvegardeCallback(() -> {
                chargerTousUsers();
                chargerStatistiques();
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    // ── Supprimer ─────────────────────────────────────────────────
    private void supprimerUser(User u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer " + u.getPrenom()
                + " " + u.getNom() + " ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (dao.delete(u.getId())) {
                    chargerTousUsers();
                    chargerStatistiques();
                } else {
                    showAlert("Erreur", "Échec de la suppression.");
                }
            }
        });
    }

    // ── Recherche ─────────────────────────────────────────────────
    @FXML
    private void handleRecherche() {
        String kw = txtRecherche.getText().trim();
        if (kw.isEmpty()) { chargerTousUsers(); return; }
        tableUsers.setItems(
                FXCollections.observableArrayList(dao.search(kw)));
    }

    @FXML
    private void handleActualiser() {
        txtRecherche.clear();
        chargerTousUsers();
    }

    // ── Déconnexion ───────────────────────────────────────────────
    @FXML
    private void handleDeconnexion() {
        SessionManager.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/login.fxml"));
            Stage stage = new Stage();
            stage.setTitle("EduNova - Connexion");
            stage.setScene(new Scene(loader.load()));
            stage.show();
            ((Stage) lblPageTitre.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}