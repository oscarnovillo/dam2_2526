package org.example.spring.ui.dto;

/**
 * Request para verificar código TOTP durante el login
 */
public record Verify2FARequest(
    String username,
    String code  // Código TOTP de 6 dígitos
) {}

