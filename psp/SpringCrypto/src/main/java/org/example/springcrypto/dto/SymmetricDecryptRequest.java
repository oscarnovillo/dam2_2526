package org.example.springcrypto.dto;

/**
 * Request para desencriptación simétrica
 */
public record SymmetricDecryptRequest(
        String encryptedText,
        String key,           // Base64 encoded key
        String mode          // ECB, CBC, CTR, GCM
) {}

