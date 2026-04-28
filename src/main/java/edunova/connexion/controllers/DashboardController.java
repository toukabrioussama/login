package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;
import edunova.connexion.tools.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // ── NAVBAR ────────────────────────────────────────────────────
    @FXML private Label     lblPageTitre;
    @FXML private Label     lblDateHeure;
    @FXML private Label     lblUserNom;
    @FXML private Label     lblUserRole;
    @FXML private Label     lblAvatarInitiales;
    @FXML private TextField txtRechercheGlobal;

    // ── PAGES ─────────────────────────────────────────────────────
    @FXML private ScrollPane pageDashboard;
    @FXML private VBox       pageUsers;

    // ── STATS ─────────────────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalAdmins;
    @FXML private Label lblTotalEnseignants;
    @FXML private Label lblTotalEtudiants;
    @FXML private Label lblEvolutionUsers;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;

    // ── TABLE DASHBOARD ───────────────────────────────────────────
    @FXML private TableView<User>            tableDerniers;
    @FXML private TableColumn<User, String>  dColNom;
    @FXML private TableColumn<User, String>  dColEmail;
    @FXML private TableColumn<User, String>  dColRole;
    @FXML private TableColumn<User, Boolean> dColActif;

    // ── CARTES USERS ──────────────────────────────────────────────
    @FXML private FlowPane flowCartes;
    @FXML private Label    lblCompteurUsers;
    @FXML private TextField txtRecherche;

    // ── BOUTONS MENU ──────────────────────────────────────────────
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuUsers;
    @FXML private Button btnMenuEtudiants;
    @FXML private Button btnMenuEnseignants;
    @FXML private Button btnMenuClasses;

    private final UserDAO dao = new UserDAO();

    // ── STYLES SIDEBAR ────────────────────────────────────────────
    private static final String STYLE_ACTIF =
            "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                    "-fx-font-size: 13; -fx-background-radius: 8;" +
                    "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";

    private static final String STYLE_NORMAL =
            "-fx-background-color: transparent; -fx-text-fill: #94a3b8;" +
                    "-fx-font-size: 13; -fx-background-radius: 8;" +
                    "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT; -fx-cursor: hand;";

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        configurerSession();
        configurerDate();
        configurerTableDashboard();
        chargerStatistiques();
        chargerTousUsers();
    }

    // ── Session ───────────────────────────────────────────────────
    private void configurerSession() {
        SessionManager s = SessionManager.getInstance();
        lblUserNom.setText(s.getEmail());
        lblUserRole.setText(s.getRole());
        lblAvatarInitiales.setText(
                s.getEmail().substring(0, 1).toUpperCase());
    }

    // ── Date ──────────────────────────────────────────────────────
    private void configurerDate() {
        lblDateHeure.setText(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    // ── Table dashboard ───────────────────────────────────────────
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

    // ── Statistiques ──────────────────────────────────────────────
    private void chargerStatistiques() {
        List<User> tous = dao.findAll();

        long total       = tous.size();
        long admins      = tous.stream()
                .filter(u -> "Administrateur".equals(u.getRoleNom())).count();
        long enseignants = tous.stream()
                .filter(u -> "Enseignant".equals(u.getRoleNom())).count();
        long etudiants   = tous.stream()
                .filter(u -> "Etudiant".equals(u.getRoleNom())).count();
        long actifs      = tous.stream().filter(User::isActif).count();
        long inactifs    = total - actifs;

        lblTotalUsers.setText(String.valueOf(total));
        lblTotalAdmins.setText(String.valueOf(admins));
        lblTotalEnseignants.setText(String.valueOf(enseignants));
        lblTotalEtudiants.setText(String.valueOf(etudiants));
        lblEvolutionUsers.setText(actifs + " comptes actifs");
        lblActifs.setText(String.valueOf(actifs));
        lblInactifs.setText(String.valueOf(inactifs));

        tableDerniers.setItems(
                FXCollections.observableArrayList(
                        tous.stream().limit(5).toList()));
    }

    // ── Charger cartes ────────────────────────────────────────────
    private void chargerTousUsers() {
        afficherCartes(dao.findAll());
    }

    private void afficherCartes(List<User> users) {
        flowCartes.getChildren().clear();
        lblCompteurUsers.setText(users.size() + " utilisateur(s)");
        for (User u : users) {
            flowCartes.getChildren().add(creerCarte(u));
        }
    }

    // ── Créer une carte ───────────────────────────────────────────
    private VBox creerCarte(User u) {
        String couleur, emoji;
        switch (u.getRoleNom() != null ? u.getRoleNom() : "") {
            case "Administrateur" -> { couleur = "#7c3aed"; emoji = "🛡️"; }
            case "Enseignant"     -> { couleur = "#0ea5e9"; emoji = "👨‍🏫"; }
            case "Etudiant"       -> { couleur = "#10b981"; emoji = "🎓"; }
            default               -> { couleur = "#64748b"; emoji = "👤"; }
        }

        VBox carte = new VBox(12);
        carte.setPrefWidth(220);
        carte.setMaxWidth(220);
        carte.setStyle(styleCarteNormal(couleur));

        // Avatar
        String initiales = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty())
            initiales += u.getPrenom().substring(0, 1).toUpperCase();
        if (u.getNom() != null && !u.getNom().isEmpty())
            initiales += u.getNom().substring(0, 1).toUpperCase();

        StackPane avatar = new StackPane();
        avatar.setMinSize(56, 56);
        avatar.setMaxSize(56, 56);
        avatar.setStyle("-fx-background-color:" + couleur +
                "; -fx-background-radius: 50;");
        Label lblInit = new Label(initiales);
        lblInit.setStyle(
                "-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(lblInit);

        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 20;");

        HBox header = new HBox(10, avatar, lblEmoji);
        header.setAlignment(Pos.CENTER_LEFT);

        // Infos
        Label lblNom = new Label(u.getPrenom() + " " + u.getNom());
        lblNom.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
        lblNom.setWrapText(true);

        Label lblEmail = new Label(u.getEmail());
        lblEmail.setStyle("-fx-font-size: 11; -fx-text-fill: #64748b;");
        lblEmail.setWrapText(true);

        String tel = (u.getTelephone() != null && !u.getTelephone().isEmpty())
                ? u.getTelephone() : "Pas de téléphone";
        Label lblTel = new Label("📞 " + tel);
        lblTel.setStyle("-fx-font-size: 11; -fx-text-fill: #94a3b8;");

        // Badges
        Label lblRole = new Label(u.getRoleNom());
        lblRole.setStyle(
                "-fx-background-color:" + couleur + "22;" +
                        "-fx-text-fill:" + couleur + ";" +
                        "-fx-background-radius: 20; -fx-padding: 3 12;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;");

        Label lblStatut = new Label(u.isActif() ? "● Actif" : "● Inactif");
        lblStatut.setStyle(
                "-fx-text-fill:" + (u.isActif() ? "#22c55e" : "#f87171") + ";" +
                        "-fx-font-size: 11; -fx-font-weight: bold;");

        HBox badges = new HBox(8, lblRole, lblStatut);
        badges.setAlignment(Pos.CENTER_LEFT);

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2d2d4e;");
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);

        // Boutons
        Button btnMod = new Button("✏ Modifier");
        btnMod.setMaxWidth(Double.MAX_VALUE);
        btnMod.setStyle(
                "-fx-background-color: #2d1b69; -fx-text-fill: #a78bfa;" +
                        "-fx-background-radius: 7; -fx-padding: 7 0;" +
                        "-fx-font-size: 12; -fx-cursor: hand;");

        Button btnDel = new Button("🗑 Supprimer");
        btnDel.setMaxWidth(Double.MAX_VALUE);
        btnDel.setStyle(
                "-fx-background-color: #2d1b1b; -fx-text-fill: #f87171;" +
                        "-fx-background-radius: 7; -fx-padding: 7 0;" +
                        "-fx-font-size: 12; -fx-cursor: hand;");

        HBox btns = new HBox(8, btnMod, btnDel);
        HBox.setHgrow(btnMod, Priority.ALWAYS);
        HBox.setHgrow(btnDel, Priority.ALWAYS);

        btnMod.setOnAction(e -> ouvrirFormulaire(u));
        btnDel.setOnAction(e -> supprimerUser(u));

        // Hover
        final String couleurFinal = couleur;
        carte.setOnMouseEntered(e ->
                carte.setStyle(styleCarteHover(couleurFinal)));
        carte.setOnMouseExited(e ->
                carte.setStyle(styleCarteNormal(couleurFinal)));

        carte.getChildren().addAll(
                header, lblNom, lblEmail, lblTel, badges, sep, btns);
        return carte;
    }

    private String styleCarteNormal(String c) {
        return "-fx-background-color: #1a1a2e; -fx-background-radius: 14;" +
                "-fx-padding: 20; -fx-border-color:" + c + ";" +
                "-fx-border-radius: 14; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.4),10,0,0,4);";
    }

    private String styleCarteHover(String c) {
        return "-fx-background-color: #1e1e38; -fx-background-radius: 14;" +
                "-fx-padding: 20; -fx-border-color:" + c + ";" +
                "-fx-border-radius: 14; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian," + c + "66,20,0,0,0);";
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void handleMenuDashboard()  { afficherPage("dashboard"); }
    @FXML private void handleMenuUsers()      { afficherPage("users"); }
    @FXML private void handleMenuEtudiants()  {
        showInfo("Module Etudiants — bientôt disponible !"); }
    @FXML private void handleMenuEnseignants() {
        showInfo("Module Enseignants — bientôt disponible !"); }
    @FXML private void handleMenuClasses()    {
        showInfo("Module Classes — bientôt disponible !"); }

    private void afficherPage(String page) {
        pageDashboard.setVisible(false); pageDashboard.setManaged(false);
        pageUsers.setVisible(false);     pageUsers.setManaged(false);

        btnMenuDashboard.setStyle(STYLE_NORMAL);
        btnMenuUsers.setStyle(STYLE_NORMAL);

        switch (page) {
            case "dashboard" -> {
                pageDashboard.setVisible(true); pageDashboard.setManaged(true);
                lblPageTitre.setText("Tableau de bord");
                btnMenuDashboard.setStyle(STYLE_ACTIF);
                chargerStatistiques();
            }
            case "users" -> {
                pageUsers.setVisible(true); pageUsers.setManaged(true);
                lblPageTitre.setText("Gestion des Utilisateurs");
                btnMenuUsers.setStyle(STYLE_ACTIF);
                chargerTousUsers();
            }
        }
    }

    // ── Formulaire ────────────────────────────────────────────────
    @FXML
    private void handleAjouterUser() { ouvrirFormulaire(null); }

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
            if (userAModifier == null) ctrl.configurerAjout();
            else                       ctrl.configurerModification(userAModifier);

            ctrl.setOnSauvegardeCallback(() -> {
                chargerTousUsers();
                chargerStatistiques();
            });
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Supprimer ─────────────────────────────────────────────────
    private void supprimerUser(User u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Supprimer " + u.getPrenom() + " " + u.getNom() + " ?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (dao.delete(u.getId())) {
                    chargerTousUsers();
                    chargerStatistiques();
                } else {
                    showInfo("Échec de la suppression.");
                }
            }
        });
    }

    // ── Recherche ─────────────────────────────────────────────────
    @FXML
    private void handleRecherche() {
        String kw = txtRecherche.getText().trim();
        if (kw.isEmpty()) { afficherCartes(dao.findAll()); return; }
        afficherCartes(dao.search(kw));
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}