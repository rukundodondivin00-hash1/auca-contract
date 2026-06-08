package com.auca.contractsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 1. Allow your React frontend URLs
        // Vite defaults to 5173. Add your production Render URL here later!
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173", 
                "http://localhost:3000"
        ));
        
        // 2. Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 3. Allow specific headers (Crucial for passing the JWT token)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // 4. Allow credentials (useful if you ever switch to secure cookies)
        configuration.setAllowCredentials(true);

        // 5. Apply this configuration to all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}