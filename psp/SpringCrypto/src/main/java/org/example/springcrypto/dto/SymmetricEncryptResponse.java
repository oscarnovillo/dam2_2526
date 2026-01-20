package org.example.springcrypto.dto;

/**
 * Response para operaciones de encriptación simétrica
 */
public record SymmetricEncryptResponse(
        String encryptedText,
        String key,           // Base64 encoded key
        String mode,
        String iv             // Base64 encoded IV (si aplica)
) {}

