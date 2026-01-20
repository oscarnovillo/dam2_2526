package org.example.springcrypto.dto;

/**
 * Request para desencriptación híbrida
 */
public record HybridDecryptRequest(
        String encryptedData,
        String encryptedKey,
        String iv,
        String privateKey     // Base64 encoded
) {}

