package com.auca.contractsystem.controller;

import com.auca.contractsystem.client.AucaApiClient;
import com.auca.contractsystem.dto.AucaTermResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Proxy controller that forwards requests to the AUCA IMS backend.
 * This avoids CORS issues for the Vercel-hosted frontend, since it calls
 * this backend (which has CORS configured) instead of the IMS directly.
 */
@RestController
@RequestMapping("/api/proxy")
@RequiredArgsConstructor
@Tag(name = "Proxy", description = "Proxy endpoints to AUCA IMS backend")
public class ProxyController {

    private final AucaApiClient aucaApiClient;

    @GetMapping("/term")
    @Operation(summary = "Get active academic term from IMS")
    public ResponseEntity<AucaTermResponse> getActiveTerm() {
        AucaTermResponse term = aucaApiClient.getActiveTerm();
        if (term == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(term);
    }
}
