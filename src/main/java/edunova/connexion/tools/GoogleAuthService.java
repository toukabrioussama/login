package edunova.connexion.tools;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.util.Arrays;
import java.util.List;

public class GoogleAuthService {

    private static final String CLIENT_ID =
            "506863117414-31gv071h11cj8qr88qio7b924u8j36ii" +
                    ".apps.googleusercontent.com";

    private static final String CLIENT_SECRET =
            "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";

    private static final String APPLICATION_NAME = "EduNova";

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    public static Userinfo getGoogleUserInfo() throws Exception {
        final NetHttpTransport transport =
                GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets =
                new GoogleClientSecrets()
                        .setInstalled(
                                new GoogleClientSecrets.Details()
                                        .setClientId(CLIENT_ID)
                                        .setClientSecret(CLIENT_SECRET));

        // ── FIX : MemoryDataStore → pas de token sauvegardé ──────
        // Chaque connexion force une nouvelle authentification
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        transport,
                        JSON_FACTORY,
                        clientSecrets,
                        SCOPES)
                        // FIX : mémoire au lieu de fichier
                        .setDataStoreFactory(
                                new MemoryDataStoreFactory())
                        // FIX : pas de refresh token
                        .setAccessType("online")
                        .build();

        // FIX : identifiant unique par session
        // pour forcer un nouveau login à chaque fois
        String sessionId = "session_" +
                System.currentTimeMillis();

        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder()
                        .setPort(8888)
                        .build();

        Credential credential =
                new AuthorizationCodeInstalledApp(
                        flow, receiver)
                        .authorize(sessionId); // ← unique à chaque fois

        Oauth2 oauth2Service = new Oauth2.Builder(
                transport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        return oauth2Service.userinfo().get().execute();
    }
}