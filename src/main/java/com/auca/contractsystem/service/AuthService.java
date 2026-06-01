package com.auca.contractsystem.service;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.*;
import com.auca.contractsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AucaApiClient aucaApiClient;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        AucaLoginResponse aucaResponse = aucaApiClient.authenticate(
            request.getUsername(), request.getPassword());
        String token = jwtUtil.generateToken(aucaResponse.getUsername(), aucaResponse.getRole());
        log.info("Login successful for user: {}", request.getUsername());
        return LoginResponse.builder()
            .token(token)
            .username(aucaResponse.getUsername())
            .fullName(aucaResponse.getFullName())
            .email(aucaResponse.getEmail())
            .role(aucaResponse.getRole())
            .build();
    }
}
