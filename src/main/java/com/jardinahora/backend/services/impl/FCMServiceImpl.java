package com.jardinahora.backend.services.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.jardinahora.backend.services.FCMService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementação do envio de notificações via Firebase Cloud Messaging.
 * Funciona apenas se as credenciais do Firebase estiverem configuradas
 * (ver ACÕES_NECESSARIAS_USUARIO.md).
 */
@Service
@Slf4j
public class FCMServiceImpl implements FCMService {

    @Value("${app.firebase.enabled:false}")
    private boolean enabled;

    /**
     * Caminho do arquivo JSON de credenciais do Firebase (Service Account).
     * Ex: file:./firebase-credentials.json ou classpath:firebase-credentials.json
     */
    @Value("${app.firebase.credentials-path:}")
    private String credentialsPath;

    /**
     * Conteúdo do JSON de credenciais em Base64 (alternativa ao arquivo).
     * Útil para variável de ambiente: FIREBASE_CREDENTIALS_JSON
     */
    @Value("${app.firebase.credentials-json-base64:}")
    private String credentialsJsonBase64;

    private FirebaseMessaging firebaseMessaging;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("FCM desabilitado (app.firebase.enabled=false). Notificações push serão apenas registradas no banco.");
            return;
        }
        try {
            InputStream credentialsStream = getCredentialsStream();
            if (credentialsStream == null) {
                log.warn("FCM habilitado mas credenciais não encontradas. Configure app.firebase.credentials-path ou app.firebase.credentials-json-base64.");
                enabled = false;
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            this.firebaseMessaging = FirebaseMessaging.getInstance();
            log.info("Firebase Cloud Messaging inicializado com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao inicializar Firebase. FCM desabilitado. Verifique as credenciais.", e);
            enabled = false;
        }
    }

    private InputStream getCredentialsStream() throws Exception {
        if (credentialsJsonBase64 != null && !credentialsJsonBase64.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(credentialsJsonBase64);
            return new ByteArrayInputStream(decoded);
        }
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            String path = credentialsPath.trim();
            if (path.startsWith("classpath:")) {
                String resource = path.substring("classpath:".length()).trim();
                return new ClassPathResource(resource).getInputStream();
            }
            if (path.startsWith("file:")) {
                path = path.substring("file:".length()).trim();
            }
            return new FileInputStream(path);
        }
        return null;
    }

    @Override
    public boolean isEnabled() {
        return enabled && firebaseMessaging != null;
    }

    @Override
    public boolean sendToToken(String deviceToken, String title, String body, Map<String, String> data) {
        if (!isEnabled()) {
            log.debug("FCM não está habilitado; notificação não enviada para token.");
            return false;
        }
        try {
            Message.Builder builder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }
            firebaseMessaging.send(builder.build());
            log.debug("Notificação FCM enviada para token com sucesso.");
            return true;
        } catch (FirebaseMessagingException e) {
            log.warn("Falha ao enviar FCM para token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int sendToTokens(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        if (!isEnabled() || deviceTokens == null || deviceTokens.isEmpty()) {
            return 0;
        }
        int sent = 0;
        for (String token : deviceTokens) {
            if (sendToToken(token, title, body, data)) {
                sent++;
            }
        }
        return sent;
    }
}
