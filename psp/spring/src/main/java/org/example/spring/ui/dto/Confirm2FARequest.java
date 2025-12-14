package org.example.spring.ui.dto;

/**
 * Request para confirmar/activar 2FA después de escanear el QR
 */
public record Confirm2FARequest(
    String code  // Código TOTP de 6 dígitos
) {}

