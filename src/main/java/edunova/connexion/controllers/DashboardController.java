package edunova.connexion.controllers;

import edunova.connexion.dao.UserDAO;
import edunova.connexion.models.User;
import edunova.connexion.tools.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    // ── ROOT ──────────────────────────────────────────────────────
    @FXML private BorderPane rootPane;

    // ── SIDEBAR ───────────────────────────────────────────────────
    @FXML private VBox    sidebar;
    @FXML private VBox    logoBox;
    @FXML private VBox    userBox;
    @FXML private VBox    menuBox;
    @FXML private VBox    bottomBox;
    @FXML private Region  sepLogo;
    @FXML private Region  sepUser;
    @FXML private Label   lblLogo;
    @FXML private Label   lblLogoSub;
    @FXML private Label   lblAvatarInitiales;
    @FXML private Label   lblUserNom;
    @FXML private Label   lblUserRole;
    @FXML private StackPane avatarPane;
    @FXML private Label   lblTheme;
    @FXML private Button  btnToggleTheme;
    @FXML private Button  btnDeconnexion;

    // ── MENU BUTTONS ──────────────────────────────────────────────
    @FXML private Button btnMenuDashboard;
    @FXML private Button btnMenuUsers;
    @FXML private Button btnMenuEtudiants;
    @FXML private Button btnMenuEnseignants;
    @FXML private Button btnMenuClasses;

    // ── NAVBAR ────────────────────────────────────────────────────
    @FXML private HBox    navbar;
    @FXML private VBox    mainContent;
    @FXML private Label   lblPageTitre;
    @FXML private Label   lblPageSousTitre;
    @FXML private Label   lblDateHeure;
    @FXML private TextField txtRechercheGlobal;
    @FXML private Region  sepNavbar;

    // ── PAGES ─────────────────────────────────────────────────────
    @FXML private ScrollPane pageDashboard;
    @FXML private VBox       pageUsers;
    @FXML private VBox       dashContent;

    // ── STATS LABELS ──────────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalAdmins;
    @FXML private Label lblTotalEnseignants;
    @FXML private Label lblTotalEtudiants;
    @FXML private Label lblEvolutionUsers;
    @FXML private Label lblActifs;
    @FXML private Label lblInactifs;
    @FXML private Label lblVueEnsemble;
    @FXML private Label lblVueSous;
    @FXML private Label lblDerniersTitle;
    @FXML private Label lblDerniersSub;
    @FXML private Label lblActifsTitle;
    @FXML private Label lblActifsLbl;
    @FXML private Label lblInactifsLbl;
    @FXML private Label lblActionsTitle;
    @FXML private Label lblStatUsers;
    @FXML private Label lblStatAdmins;
    @FXML private Label lblStatEnseignants;
    @FXML private Label lblStatEtudiants;

    // ── CARDS DASHBOARD ───────────────────────────────────────────
    @FXML private VBox     cardUsers;
    @FXML private VBox     cardAdmins;
    @FXML private VBox     cardEnseignants;
    @FXML private VBox     cardEtudiants;
    @FXML private VBox     cardDerniers;
    @FXML private VBox     cardActifs;
    @FXML private VBox     cardActions;
    @FXML private Button   btnGererUsers;
    @FXML private FlowPane flowDerniers;

    // ── PAGE USERS ────────────────────────────────────────────────
    @FXML private FlowPane  flowCartes;
    @FXML private Label     lblCompteurUsers;
    @FXML private TextField txtRecherche;
    @FXML private Label     lblUsersGrand;
    @FXML private Label     lblUsersSous;

    private final UserDAO dao     = new UserDAO();
    private       boolean isDark  = true;

    // ══════════════════════════════════════════════════════════════
    //  THÈMES
    // ══════════════════════════════════════════════════════════════

    // Couleurs DARK
    private static final String D_BG_MAIN    = "#0f0f1a";
    private static final String D_BG_SIDEBAR = "#1a1a2e";
    private static final String D_BG_CARD    = "#1a1a2e";
    private static final String D_BG_NAVBAR  = "#1a1a2e";
    private static final String D_BORDER     = "#2d2d4e";
    private static final String D_TEXT_MAIN  = "#e2e8f0";
    private static final String D_TEXT_SUB   = "#64748b";
    private static final String D_TEXT_MENU  = "#94a3b8";

    // Couleurs LIGHT
    private static final String L_BG_MAIN    = "#f1f5f9";
    private static final String L_BG_SIDEBAR = "#ffffff";
    private static final String L_BG_CARD    = "#ffffff";
    private static final String L_BG_NAVBAR  = "#ffffff";
    private static final String L_BORDER     = "#e2e8f0";
    private static final String L_TEXT_MAIN  = "#1e293b";
    private static final String L_TEXT_SUB   = "#64748b";
    private static final String L_TEXT_MENU  = "#475569";

    // ── INITIALISATION ────────────────────────────────────────────
    @FXML
    public void initialize() {
        configurerSession();
        configurerDate();
        appliquerTheme();
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

    // ══════════════════════════════════════════════════════════════
    //  TOGGLE THÈME
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleToggleTheme() {
        isDark = !isDark;
        appliquerTheme();
    }

    private void appliquerTheme() {
        String bgMain    = isDark ? D_BG_MAIN    : L_BG_MAIN;
        String bgSidebar = isDark ? D_BG_SIDEBAR : L_BG_SIDEBAR;
        String bgCard    = isDark ? D_BG_CARD    : L_BG_CARD;
        String bgNavbar  = isDark ? D_BG_NAVBAR  : L_BG_NAVBAR;
        String border    = isDark ? D_BORDER     : L_BORDER;
        String textMain  = isDark ? D_TEXT_MAIN  : L_TEXT_MAIN;
        String textSub   = isDark ? D_TEXT_SUB   : L_TEXT_SUB;
        String textMenu  = isDark ? D_TEXT_MENU  : L_TEXT_MENU;

        // ── Root ──────────────────────────────────────────────────
        rootPane.setStyle(
                "-fx-background-color: " + bgMain + ";");

        // ── Sidebar ───────────────────────────────────────────────
        sidebar.setStyle(
                "-fx-background-color: " + bgSidebar + ";");
        sepLogo.setStyle(
                "-fx-background-color: " + border + "; -fx-pref-height: 1;");
        sepUser.setStyle(
                "-fx-background-color: " + border + "; -fx-pref-height: 1;");

        // Logo
        lblLogo.setStyle(
                "-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: #7c3aed;");
        lblLogoSub.setStyle(
                "-fx-font-size: 10; -fx-text-fill: " +
                        (isDark ? "#a78bfa" : "#7c3aed") + "; -fx-wrap-text: true;");

        // User info
        lblUserNom.setStyle(
                "-fx-font-size: 13; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + "; -fx-padding: 8 0 2 0;");

        // Bouton thème
        lblTheme.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textMenu + ";");
        btnToggleTheme.setText(isDark ? "☀ Light" : "🌙 Dark");
        btnToggleTheme.setStyle(
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                        "-fx-background-radius: 20; -fx-padding: 5 16;" +
                        "-fx-font-size: 12; -fx-cursor: hand;");

        // Boutons menu sidebar
        appliquerStyleMenuBoutons(textMenu, border);

        // ── Navbar ────────────────────────────────────────────────
        navbar.setStyle(
                "-fx-background-color: " + bgNavbar + "; -fx-padding: 15 25;");
        sepNavbar.setStyle(
                "-fx-background-color: " + border + "; -fx-pref-height: 1;");

        lblPageTitre.setStyle(
                "-fx-font-size: 26; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblPageSousTitre.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
        lblDateHeure.setStyle(
                "-fx-text-fill: " + textSub + ";" +
                        "-fx-font-size: 12; -fx-padding: 0 0 0 20;");

        txtRechercheGlobal.setStyle(
                "-fx-background-color: " + bgMain + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 20; -fx-padding: 8 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 20;");

        // ── Main content ──────────────────────────────────────────
        mainContent.setStyle(
                "-fx-background-color: " + bgMain + ";");

        // ── Dashboard content ─────────────────────────────────────
        pageDashboard.setStyle(
                "-fx-background: " + bgMain + ";" +
                        "-fx-background-color: " + bgMain + ";");
        dashContent.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");

        // Titres section
        lblVueEnsemble.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblVueSous.setStyle(
                "-fx-font-size: 13; -fx-text-fill: " + textSub + ";");

        // Labels stats
        lblStatUsers.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblStatAdmins.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblStatEnseignants.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblStatEtudiants.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");
        lblTotalUsers.setStyle(
                "-fx-font-size: 28; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");

        // Cartes dashboard
        String styleCard =
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-background-radius: 12; -fx-padding: 20;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 12; -fx-border-width: 1;";

        cardUsers.setStyle(styleCard);
        cardDerniers.setStyle(styleCard);
        cardActifs.setStyle(styleCard);
        cardActions.setStyle(styleCard);

        // Labels cartes
        lblDerniersTitle.setStyle(
                "-fx-font-size: 16; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblDerniersSub.setStyle(
                "-fx-font-size: 11; -fx-text-fill: " + textSub + ";");
        lblActifsTitle.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-padding: 0 0 15 0;");
        lblActifsLbl.setStyle("-fx-text-fill: " + textSub + ";");
        lblInactifsLbl.setStyle("-fx-text-fill: " + textSub + ";");
        lblActionsTitle.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-padding: 0 0 15 0;");

        // Bouton gérer users
        btnGererUsers.setStyle(
                "-fx-background-color: " + bgMain + ";" +
                        "-fx-text-fill: " + textMenu + ";" +
                        "-fx-background-radius: 8; -fx-padding: 10;" +
                        "-fx-font-size: 13; -fx-border-color: " + border + ";" +
                        "-fx-border-radius: 8; -fx-cursor: hand;");

        // ── Page users ────────────────────────────────────────────
        pageUsers.setStyle(
                "-fx-background-color: " + bgMain + "; -fx-padding: 25;");

        lblUsersGrand.setStyle(
                "-fx-font-size: 22; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblUsersSous.setStyle(
                "-fx-font-size: 12; -fx-text-fill: " + textSub + ";");
        lblCompteurUsers.setStyle(
                "-fx-text-fill: " + textSub + "; -fx-font-size: 12;");

        txtRecherche.setStyle(
                "-fx-background-color: " + bgCard + ";" +
                        "-fx-text-fill: " + textMain + ";" +
                        "-fx-prompt-text-fill: " + textSub + ";" +
                        "-fx-background-radius: 8; -fx-padding: 9 15;" +
                        "-fx-border-color: " + border + "; -fx-border-radius: 8;");

        // Rafraîchir les cartes avec le nouveau thème
        chargerTousUsers();
        afficherCartesDerniers(dao.findAll().stream().limit(5).toList());
    }

    // ── Styles boutons menu sidebar ───────────────────────────────
    private void appliquerStyleMenuBoutons(String textMenu, String border) {
        Button[] boutons = {
                btnMenuDashboard, btnMenuUsers, btnMenuEtudiants,
                btnMenuEnseignants, btnMenuClasses
        };
        for (Button b : boutons) {
            b.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + textMenu + ";" +
                            "-fx-font-size: 13; -fx-background-radius: 8;" +
                            "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                            "-fx-cursor: hand;");
        }
        // Réappliquer l'actif
        btnMenuDashboard.setStyle(
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;");
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

        afficherCartesDerniers(tous.stream().limit(5).toList());
    }

    // ── Cartes mini dashboard ─────────────────────────────────────
    private void afficherCartesDerniers(List<User> users) {
        flowDerniers.getChildren().clear();

        String bgMini = isDark ? "#0f0f1a" : "#f8fafc";

        for (User u : users) {
            String couleur, emoji;
            switch (u.getRoleNom() != null ? u.getRoleNom() : "") {
                case "Administrateur" -> { couleur = "#7c3aed"; emoji = "🛡️"; }
                case "Enseignant"     -> { couleur = "#0ea5e9"; emoji = "👨‍🏫"; }
                case "Etudiant"       -> { couleur = "#10b981"; emoji = "🎓"; }
                default               -> { couleur = "#64748b"; emoji = "👤"; }
            }

            HBox carte = new HBox(12);
            carte.setAlignment(Pos.CENTER_LEFT);
            carte.setPrefWidth(280);
            carte.setStyle(styleMiniNormal(couleur, bgMini));

            // Avatar
            String initiales = "";
            if (u.getPrenom() != null && !u.getPrenom().isEmpty())
                initiales += u.getPrenom().substring(0, 1).toUpperCase();
            if (u.getNom() != null && !u.getNom().isEmpty())
                initiales += u.getNom().substring(0, 1).toUpperCase();

            StackPane avatar = new StackPane();
            avatar.setMinSize(40, 40);
            avatar.setMaxSize(40, 40);
            avatar.setStyle(
                    "-fx-background-color: " + couleur + ";" +
                            "-fx-background-radius: 50;");
            Label lblInit = new Label(initiales);
            lblInit.setStyle(
                    "-fx-font-size: 14; -fx-font-weight: bold;" +
                            "-fx-text-fill: white;");
            avatar.getChildren().add(lblInit);

            VBox infos = new VBox(3);
            infos.setAlignment(Pos.CENTER_LEFT);

            String textColor = isDark ? "#e2e8f0" : "#1e293b";

            Label lblNom = new Label(u.getPrenom() + " " + u.getNom());
            lblNom.setStyle(
                    "-fx-font-size: 13; -fx-font-weight: bold;" +
                            "-fx-text-fill: " + textColor + ";");

            HBox ligne2 = new HBox(8);
            ligne2.setAlignment(Pos.CENTER_LEFT);

            Label lblEmoji = new Label(emoji);
            lblEmoji.setStyle("-fx-font-size: 12;");

            Label lblRole = new Label(u.getRoleNom());
            lblRole.setStyle(
                    "-fx-font-size: 11; -fx-font-weight: bold;" +
                            "-fx-text-fill: " + couleur + ";");

            Label lblSep = new Label("•");
            lblSep.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");

            Label lblStatut = new Label(u.isActif() ? "● Actif" : "● Inactif");
            lblStatut.setStyle(
                    "-fx-font-size: 11; -fx-font-weight: bold;" +
                            "-fx-text-fill: " +
                            (u.isActif() ? "#22c55e" : "#f87171") + ";");

            ligne2.getChildren().addAll(lblEmoji, lblRole, lblSep, lblStatut);
            infos.getChildren().addAll(lblNom, ligne2);

            final String c = couleur;
            final String bg = bgMini;
            carte.setOnMouseEntered(e -> carte.setStyle(styleMiniHover(c, bg)));
            carte.setOnMouseExited(e  -> carte.setStyle(styleMiniNormal(c, bg)));
            carte.setOnMouseClicked(e -> afficherPage("users"));

            carte.getChildren().addAll(avatar, infos);
            flowDerniers.getChildren().add(carte);
        }
    }

    private String styleMiniNormal(String c, String bg) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 10; -fx-padding: 12 15;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 10; -fx-border-width: 1;" +
                "-fx-cursor: hand;";
    }

    private String styleMiniHover(String c, String bg) {
        String bgHover = isDark ? "#1a1a2e" : "#f0f4ff";
        return "-fx-background-color: " + bgHover + ";" +
                "-fx-background-radius: 10; -fx-padding: 12 15;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 10; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian," + c + "66,12,0,0,0);" +
                "-fx-cursor: hand;";
    }

    // ── Cartes utilisateurs ───────────────────────────────────────
    private void chargerTousUsers() {
        afficherCartes(dao.findAll());
    }

    private void afficherCartes(List<User> users) {
        flowCartes.getChildren().clear();
        if (lblCompteurUsers != null)
            lblCompteurUsers.setText(users.size() + " utilisateur(s)");
        for (User u : users)
            flowCartes.getChildren().add(creerCarte(u));
    }

    private VBox creerCarte(User u) {
        String couleur, emoji;
        switch (u.getRoleNom() != null ? u.getRoleNom() : "") {
            case "Administrateur" -> { couleur = "#7c3aed"; emoji = "🛡️"; }
            case "Enseignant"     -> { couleur = "#0ea5e9"; emoji = "👨‍🏫"; }
            case "Etudiant"       -> { couleur = "#10b981"; emoji = "🎓"; }
            default               -> { couleur = "#64748b"; emoji = "👤"; }
        }

        String bgCard   = isDark ? "#1a1a2e" : "#ffffff";
        String textMain = isDark ? "#e2e8f0" : "#1e293b";
        String textSub  = isDark ? "#64748b" : "#94a3b8";
        String sepColor = isDark ? "#2d2d4e" : "#e2e8f0";

        VBox carte = new VBox(12);
        carte.setPrefWidth(220);
        carte.setMaxWidth(220);
        carte.setStyle(styleCarteNormal(couleur, bgCard));

        // Avatar
        String initiales = "";
        if (u.getPrenom() != null && !u.getPrenom().isEmpty())
            initiales += u.getPrenom().substring(0, 1).toUpperCase();
        if (u.getNom() != null && !u.getNom().isEmpty())
            initiales += u.getNom().substring(0, 1).toUpperCase();

        StackPane avatar = new StackPane();
        avatar.setMinSize(56, 56);
        avatar.setMaxSize(56, 56);
        avatar.setStyle(
                "-fx-background-color: " + couleur + ";" +
                        "-fx-background-radius: 50;");
        Label lblInit = new Label(initiales);
        lblInit.setStyle(
                "-fx-font-size: 20; -fx-font-weight: bold;" +
                        "-fx-text-fill: white;");
        avatar.getChildren().add(lblInit);

        Label lblEmoji = new Label(emoji);
        lblEmoji.setStyle("-fx-font-size: 20;");

        HBox header = new HBox(10, avatar, lblEmoji);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblNom = new Label(u.getPrenom() + " " + u.getNom());
        lblNom.setStyle(
                "-fx-font-size: 14; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + textMain + ";");
        lblNom.setWrapText(true);

        Label lblEmail = new Label(u.getEmail());
        lblEmail.setStyle(
                "-fx-font-size: 11; -fx-text-fill: " + textSub + ";");
        lblEmail.setWrapText(true);

        String tel = (u.getTelephone() != null && !u.getTelephone().isEmpty())
                ? u.getTelephone() : "Pas de téléphone";
        Label lblTel = new Label("📞 " + tel);
        lblTel.setStyle(
                "-fx-font-size: 11; -fx-text-fill: " + textSub + ";");

        Label lblRole = new Label(u.getRoleNom());
        lblRole.setStyle(
                "-fx-background-color: " + couleur + "22;" +
                        "-fx-text-fill: " + couleur + ";" +
                        "-fx-background-radius: 20; -fx-padding: 3 12;" +
                        "-fx-font-size: 11; -fx-font-weight: bold;");

        Label lblStatut = new Label(u.isActif() ? "● Actif" : "● Inactif");
        lblStatut.setStyle(
                "-fx-text-fill: " + (u.isActif() ? "#22c55e" : "#f87171") + ";" +
                        "-fx-font-size: 11; -fx-font-weight: bold;");

        HBox badges = new HBox(8, lblRole, lblStatut);
        badges.setAlignment(Pos.CENTER_LEFT);

        Region sep = new Region();
        sep.setStyle("-fx-background-color: " + sepColor + ";");
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);

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

        final String c  = couleur;
        final String bg = bgCard;
        carte.setOnMouseEntered(e -> carte.setStyle(styleCarteHover(c, bg)));
        carte.setOnMouseExited(e  -> carte.setStyle(styleCarteNormal(c, bg)));

        carte.getChildren().addAll(
                header, lblNom, lblEmail, lblTel, badges, sep, btns);
        return carte;
    }

    private String styleCarteNormal(String c, String bg) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 14; -fx-padding: 20;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 14; -fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.15),10,0,0,4);";
    }

    private String styleCarteHover(String c, String bg) {
        String bgHover = isDark ? "#1e1e38" : "#f0f4ff";
        return "-fx-background-color: " + bgHover + ";" +
                "-fx-background-radius: 14; -fx-padding: 20;" +
                "-fx-border-color: " + c + ";" +
                "-fx-border-radius: 14; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian," + c + "66,20,0,0,0);";
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void handleMenuDashboard()   { afficherPage("dashboard"); }
    @FXML private void handleMenuUsers()       { afficherPage("users"); }
    @FXML private void handleMenuEtudiants()   {
        showInfo("Module Etudiants — bientôt disponible !"); }
    @FXML private void handleMenuEnseignants() {
        showInfo("Module Enseignants — bientôt disponible !"); }
    @FXML private void handleMenuClasses()     {
        showInfo("Module Classes — bientôt disponible !"); }

    private void afficherPage(String page) {
        pageDashboard.setVisible(false); pageDashboard.setManaged(false);
        pageUsers.setVisible(false);     pageUsers.setManaged(false);

        // Reset tous les boutons
        String bgSidebar = isDark ? D_BG_SIDEBAR : L_BG_SIDEBAR;
        String textMenu  = isDark ? D_TEXT_MENU  : L_TEXT_MENU;
        String styleNormal =
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + textMenu + ";" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;";
        String styleActif =
                "-fx-background-color: #7c3aed; -fx-text-fill: white;" +
                        "-fx-font-size: 13; -fx-background-radius: 8;" +
                        "-fx-padding: 11 15; -fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;";

        btnMenuDashboard.setStyle(styleNormal);
        btnMenuUsers.setStyle(styleNormal);
        btnMenuEtudiants.setStyle(styleNormal);
        btnMenuEnseignants.setStyle(styleNormal);
        btnMenuClasses.setStyle(styleNormal);

        switch (page) {
            case "dashboard" -> {
                pageDashboard.setVisible(true);
                pageDashboard.setManaged(true);
                lblPageTitre.setText("Tableau de bord");
                lblPageSousTitre.setText(
                        "Gestion des utilisateurs — Vue d'ensemble");
                btnMenuDashboard.setStyle(styleActif);
                chargerStatistiques();
            }
            case "users" -> {
                pageUsers.setVisible(true);
                pageUsers.setManaged(true);
                lblPageTitre.setText("Utilisateurs");
                lblPageSousTitre.setText(
                        "Gérer tous les comptes de la plateforme");
                btnMenuUsers.setStyle(styleActif);
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
        } catch (Exception e) { e.printStackTrace(); }
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