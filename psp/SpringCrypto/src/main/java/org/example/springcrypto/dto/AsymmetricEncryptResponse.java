package org.example.springcrypto.dto;

/**
 * Response para encriptación asimétrica
 */
public record AsymmetricEncryptResponse(
        String encryptedText,
        String padding
) {}

