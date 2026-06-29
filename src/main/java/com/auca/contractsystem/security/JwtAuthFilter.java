package com.auca.contractsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // Check if it's the frontend's mock session token (base64 JSON, no dots)
            if (!token.contains(".")) {
                try {
                    String decoded = new String(Base64.getDecoder().decode(token));
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(decoded);
                    if (node.has("username") && node.has("role")) {
                        String username = node.get("username").asText();
                        String role = node.get("role").asText();
                        // ensure role format
                        if (!role.startsWith("ROLE_")) {
                            role = "ROLE_" + role;
                        }
                        var auth = new UsernamePasswordAuthenticationToken(username, null,
                                List.of(new SimpleGrantedAuthority(role)));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        System.out.println("SUCCESSFULLY AUTHENTICATED MOCK TOKEN: " + username + " " + role);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to decode mock token: " + e.getMessage());
                }
            } else {
                // Regular JWT validation
                if (jwtUtil.isTokenValid(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }
                    var auth = new UsernamePasswordAuthenticationToken(username, null,
                            List.of(new SimpleGrantedAuthority(role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
