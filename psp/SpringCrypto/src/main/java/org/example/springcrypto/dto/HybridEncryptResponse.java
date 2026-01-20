package org.example.springcrypto.dto;

/**
 * Response para encriptación híbrida
 */
public record HybridEncryptResponse(
        String encryptedData,
        String encryptedKey,
        String iv
) {}

