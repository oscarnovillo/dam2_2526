package org.example.springcrypto.dto;

/**
 * Request para encriptación asimétrica
 */
public record AsymmetricEncryptRequest(
        String plainText,
        String publicKey,     // Base64 encoded
        String padding        // PKCS1, OAEP
) {}

