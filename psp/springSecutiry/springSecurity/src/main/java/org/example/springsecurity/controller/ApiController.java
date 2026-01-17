package org.example.springsecurity.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * Endpoint público - no requiere autenticación
     * GET /api/public
     */
    @GetMapping("/public")
    public Map<String, Object> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Este es un endpoint público");
        response.put("timestamp", LocalDateTime.now());
        response.put("autenticado", false);
        return response;
    }

    /**
     * Endpoint protegido - requiere autenticación
     * GET /api/private
     *
     * Probar con: curl -u usuario:password123 http://localhost:8080/api/private
     */
    @GetMapping("/private")
    public Map<String, Object> privateEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "¡Acceso autorizado! Este es un endpoint protegido");
        response.put("timestamp", LocalDateTime.now());
        response.put("usuario", authentication.getName());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        response.put("roles", roles);

        return response;
    }

    /**
     * Endpoint solo para ADMIN
     * GET /api/admin
     *
     * Probar con: curl -u admin:admin123 http://localhost:8080/api/admin
     */
    @GetMapping("/admin")
    public Map<String, Object> adminEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "¡Bienvenido Admin! Tienes acceso total");
        response.put("timestamp", LocalDateTime.now());
        response.put("usuario", authentication.getName());
        response.put("esAdmin", true);
        return response;
    }
}

