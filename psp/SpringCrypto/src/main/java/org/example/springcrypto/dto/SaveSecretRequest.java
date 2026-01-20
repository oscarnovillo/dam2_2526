package org.example.springcrypto.dto;

public record SaveSecretRequest(
    String encryptedData,  // Base64 encoded
    String iv,             // Base64 encoded (12 bytes para GCM)
    String salt,           // Base64 encoded (16 bytes para PBKDF2)
    String metadata        // JSON cifrado opcional (título, tags, etc.)
) {
}

