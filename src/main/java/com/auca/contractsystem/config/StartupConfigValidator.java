package com.auca.contractsystem.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StartupConfigValidator {

    private final Environment env;

    public StartupConfigValidator(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateConfig() {
        String apiKey = env.getProperty("auca.api.key");
        String apiBaseUrl = env.getProperty("auca.api.base-url");
        String dbUrl = env.getProperty("spring.datasource.url");
        String dbUser = env.getProperty("spring.datasource.username");
        String jwtSecret = env.getProperty("app.jwt.secret");

        log.info("=== Startup Configuration Validation ===");
        log.info("AUCA API Base URL: {}", apiBaseUrl);
        log.info("AUCA API Key: {}", maskSecret(apiKey));
        log.info("Database URL: {}", dbUrl);
        log.info("Database User: {}", dbUser);
        log.info("JWT Secret configured: {}", jwtSecret != null && !jwtSecret.isBlank());
        log.info("========================================");

        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_") || apiKey.equals("YOUR_API_KEY_HERE")) {
            log.error("FAIL: AUCA_API_KEY is not configured! Set the AUCA_API_KEY environment variable in Render.");
        } else {
            log.info("PASS: AUCA_API_KEY is configured");
        }

        if (dbUrl == null || dbUrl.isBlank()) {
            log.error("FAIL: Database URL is not configured!");
        } else {
            log.info("PASS: Database URL is configured");
        }
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.isBlank()) return "NOT SET";
        if (secret.length() <= 8) return "***";
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
}
