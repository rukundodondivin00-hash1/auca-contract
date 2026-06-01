package com.auca.contractsystem.client;

import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.exception.AucaApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AucaApiClient {

    private final RestTemplate restTemplate;

    @Value("${auca.api.base-url}")
    private String baseUrl;

    @Value("${auca.api.key}")
    private String apiKey;

    public AucaLoginResponse authenticate(String username, String password) {
        String url = baseUrl + "/api/v1/common/auth/signin";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = new LoginRequest();
        body.setUsername(username);
        body.setPassword(password);
        HttpEntity<LoginRequest> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<AucaLoginResponse> response = restTemplate.postForEntity(
                url, request, AucaLoginResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new AucaApiException("Authentication failed");
        } catch (Exception e) {
            log.error("AUCA auth error: {}", e.getMessage());
            throw new AucaApiException("Invalid credentials");
        }
    }

    public AucaTermResponse getActiveTerm() {
        String url = baseUrl + "/api/v1/registration/term";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ims-api-key", apiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<AucaTermResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, request, AucaTermResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new AucaApiException("Failed to fetch active term");
        } catch (Exception e) {
            log.error("AUCA term error: {}", e.getMessage());
            throw new AucaApiException("Failed to fetch active term from AUCA");
        }
    }

    public AucaRegistrationResponse getRegistration(String studentId, String termId) {
        String url = baseUrl + "/api/v1/registration/registration?studentId=" + studentId + "&termId=" + termId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ims-api-key", apiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<AucaRegistrationResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, request, AucaRegistrationResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new AucaApiException("Registration not found");
        } catch (Exception e) {
            log.error("AUCA registration error: {}", e.getMessage());
            throw new AucaApiException("Failed to fetch registration from AUCA");
        }
    }

    public AucaBalanceResponse getBalance(String studentId) {
        String url = baseUrl + "/api/v1/finance/student-payments/" + studentId + "/balance";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ims-api-key", apiKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<AucaBalanceResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, request, AucaBalanceResponse.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            throw new AucaApiException("Balance not found");
        } catch (Exception e) {
            log.error("AUCA balance error for {}: {}", studentId, e.getMessage());
            throw new AucaApiException("Failed to fetch balance from AUCA");
        }
    }
}
