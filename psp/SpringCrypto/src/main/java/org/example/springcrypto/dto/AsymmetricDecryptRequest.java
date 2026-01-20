package org.example.springcrypto.dto;

/**
 * Request para desencriptación asimétrica
 */
public record AsymmetricDecryptRequest(
        String encryptedText,
        String privateKey,    // Base64 encoded
        String padding        // PKCS1, OAEP
) {}

