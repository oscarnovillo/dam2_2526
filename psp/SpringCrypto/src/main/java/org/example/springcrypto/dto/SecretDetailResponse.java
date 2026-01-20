package org.example.springcrypto.dto;

public record SecretDetailResponse(
    Long id,
    String encryptedData,  // Base64
    String iv,             // Base64
    String salt,           // Base64
    String metadata        // JSON cifrado
) {
}

