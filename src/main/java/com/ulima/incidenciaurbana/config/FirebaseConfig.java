package com.ulima.incidenciaurbana.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 * Inicializa Firebase (Storage + Cloud Messaging) si hay credenciales. Orden:
 * 1) FIREBASE_CREDENTIALS_BASE64: el JSON de la service account en base64
 *    (lo comodo en Render: una variable de entorno, sin subir el archivo a Git).
 * 2) FIREBASE_CREDENTIALS_PATH: ruta a un archivo JSON en el servidor.
 * 3) firebase-service-account.json en src/main/resources (ignorado por Git).
 * Sin credenciales la app arranca igual: las fotos van a PostgreSQL y las
 * notificaciones push solo se registran en el log.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.base64:}")
    private String credencialesBase64;

    @Value("${firebase.credentials.path:}")
    private String credencialesPath;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try (InputStream credenciales = abrirCredenciales()) {
            if (credenciales == null) {
                log.warn("Firebase sin credenciales: fotos en PostgreSQL y push solo en el log.");
                return null;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credenciales))
                    .build();
            log.info("Firebase inicializado correctamente.");
            return FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            log.warn("No se pudo inicializar Firebase ({}). La app sigue sin Firebase.", e.getMessage());
            return null;
        }
    }

    private InputStream abrirCredenciales() throws Exception {
        if (credencialesBase64 != null && !credencialesBase64.isBlank()) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(credencialesBase64.trim()));
        }
        if (credencialesPath != null && !credencialesPath.isBlank()) {
            return new FileInputStream(credencialesPath.trim());
        }
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
        return resource.exists() ? resource.getInputStream() : null;
    }
}
