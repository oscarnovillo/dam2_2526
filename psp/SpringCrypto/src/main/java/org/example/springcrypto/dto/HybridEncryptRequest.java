package org.example.springcrypto.dto;

/**
 * Request para encriptación híbrida
 */
public record HybridEncryptRequest(
        String plainText,
        String publicKey      // Base64 encoded
) {}

