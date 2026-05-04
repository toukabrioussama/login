package edunova.connexion.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.LinkedHashMap;
import java.util.List;

public class PhonePickerController {

    // ── PAYS publique pour accès depuis autres controllers ────────
    public static final LinkedHashMap<String, String>
            PAYS = new LinkedHashMap<>();

    static {
        // Afrique du Nord & Moyen-Orient
        PAYS.put("🇹🇳 Tunisie",          "+216");
        PAYS.put("🇲🇦 Maroc",            "+212");
        PAYS.put("🇩🇿 Algérie",          "+213");
        PAYS.put("🇱🇾 Libye",            "+218");
        PAYS.put("🇪🇬 Égypte",           "+20");
        PAYS.put("🇸🇦 Arabie Saoudite",  "+966");
        PAYS.put("🇦🇪 Émirats Arabes",   "+971");
        PAYS.put("🇶🇦 Qatar",            "+974");
        PAYS.put("🇰🇼 Koweït",           "+965");
        PAYS.put("🇯🇴 Jordanie",         "+962");
        PAYS.put("🇱🇧 Liban",            "+961");
        PAYS.put("🇸🇾 Syrie",            "+963");
        PAYS.put("🇮🇶 Irak",             "+964");
        PAYS.put("🇾🇪 Yémen",            "+967");
        PAYS.put("🇴🇲 Oman",             "+968");
        PAYS.put("🇧🇭 Bahreïn",          "+973");
        PAYS.put("🇸🇩 Soudan",           "+249");
        PAYS.put("🇲🇷 Mauritanie",       "+222");
        // Europe
        PAYS.put("🇫🇷 France",           "+33");
        PAYS.put("🇩🇪 Allemagne",        "+49");
        PAYS.put("🇬🇧 Royaume-Uni",      "+44");
        PAYS.put("🇮🇹 Italie",           "+39");
        PAYS.put("🇪🇸 Espagne",          "+34");
        PAYS.put("🇧🇪 Belgique",         "+32");
        PAYS.put("🇨🇭 Suisse",           "+41");
        PAYS.put("🇳🇱 Pays-Bas",         "+31");
        PAYS.put("🇵🇹 Portugal",         "+351");
        PAYS.put("🇸🇪 Suède",            "+46");
        PAYS.put("🇳🇴 Norvège",          "+47");
        PAYS.put("🇩🇰 Danemark",         "+45");
        PAYS.put("🇵🇱 Pologne",          "+48");
        PAYS.put("🇷🇴 Roumanie",         "+40");
        PAYS.put("🇬🇷 Grèce",            "+30");
        PAYS.put("🇹🇷 Turquie",          "+90");
        PAYS.put("🇷🇺 Russie",           "+7");
        // Amériques
        PAYS.put("🇺🇸 États-Unis",       "+1");
        PAYS.put("🇨🇦 Canada",           "+1");
        PAYS.put("🇧🇷 Brésil",           "+55");
        PAYS.put("🇲🇽 Mexique",          "+52");
        PAYS.put("🇦🇷 Argentine",        "+54");
        PAYS.put("🇨🇱 Chili",            "+56");
        PAYS.put("🇨🇴 Colombie",         "+57");
        // Asie
        PAYS.put("🇨🇳 Chine",            "+86");
        PAYS.put("🇯🇵 Japon",            "+81");
        PAYS.put("🇰🇷 Corée du Sud",     "+82");
        PAYS.put("🇮🇳 Inde",             "+91");
        PAYS.put("🇵🇰 Pakistan",         "+92");
        PAYS.put("🇮🇩 Indonésie",        "+62");
        PAYS.put("🇲🇾 Malaisie",         "+60");
        PAYS.put("🇵🇭 Philippines",      "+63");
        PAYS.put("🇻🇳 Vietnam",          "+84");
        PAYS.put("🇹🇭 Thaïlande",        "+66");
        PAYS.put("🇮🇷 Iran",             "+98");
        // Afrique
        PAYS.put("🇸🇳 Sénégal",          "+221");
        PAYS.put("🇨🇮 Côte d'Ivoire",    "+225");
        PAYS.put("🇨🇲 Cameroun",         "+237");
        PAYS.put("🇬🇭 Ghana",            "+233");
        PAYS.put("🇳🇬 Nigéria",          "+234");
        PAYS.put("🇰🇪 Kenya",            "+254");
        PAYS.put("🇿🇦 Afrique du Sud",   "+27");
        PAYS.put("🇪🇹 Éthiopie",         "+251");
        // Océanie
        PAYS.put("🇦🇺 Australie",        "+61");
        PAYS.put("🇳🇿 Nouvelle-Zélande", "+64");
    }

    // ── Extraire emoji, nom, code depuis une entrée ───────────────
    public static String getEmoji(String paysNom) {
        return paysNom.split(" ")[0];
    }

    public static String getNomSansEmoji(String paysNom) {
        String[] parts = paysNom.split(" ", 2);
        return parts.length > 1 ? parts[1] : paysNom;
    }

    // ── Créer un item dropdown professionnel ──────────────────────
    public static HBox creerItem(String pays, String code,
                                 boolean dark,
                                 Runnable onSelect) {

        String bgNormal  = dark ? "transparent" : "transparent";
        String bgHover   = dark ? "#2d1b69"     : "#f5f3ff";
        String textColor = dark ? "#e2e8f0"     : "#1e293b";
        String nomColor  = dark ? "#94a3b8"     : "#64748b";
        String codeColor = dark ? "#a78bfa"     : "#7c3aed";
        String codeBg    = dark ? "#2d1b69"     : "#ede9fe";

        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle(
                "-fx-padding: 9 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: " + bgNormal + ";");

        // ── Drapeau ───────────────────────────────────────────────
        Label lblEmoji = new Label(getEmoji(pays));
        lblEmoji.setStyle("-fx-font-size: 20;");
        lblEmoji.setMinWidth(30);

        // ── Nom du pays ───────────────────────────────────────────
        Label lblNom = new Label(getNomSansEmoji(pays));
        lblNom.setStyle(
                "-fx-font-size: 12;" +
                        "-fx-text-fill: " + textColor + ";");
        HBox.setHgrow(lblNom, Priority.ALWAYS);

        // ── Badge code pays ───────────────────────────────────────
        Label lblCode = new Label(code);
        lblCode.setStyle(
                "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + codeColor + ";" +
                        "-fx-background-color: " + codeBg + ";" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 2 7;");

        item.getChildren().addAll(lblEmoji, lblNom, lblCode);

        // ── Hover ─────────────────────────────────────────────────
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-padding: 9 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: " + bgHover + ";"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-padding: 9 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: " + bgNormal + ";"));

        item.setOnMouseClicked(e -> onSelect.run());

        return item;
    }
}