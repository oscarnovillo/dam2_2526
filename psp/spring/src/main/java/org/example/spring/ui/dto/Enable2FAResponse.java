package org.example.spring.ui.dto;

/**
 * Response al habilitar 2FA, contiene el secreto y el QR code
 */
public record Enable2FAResponse(
    String secret,
    String qrCodeUri,
    String message
) {}

