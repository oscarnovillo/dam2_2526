package org.example.springcrypto.dto;

/**
 * Request para verificación de firma
 */
public record VerifyRequest(
        String message,
        String signature,     // Base64 encoded
        String publicKey      // Base64 encoded
) {}

