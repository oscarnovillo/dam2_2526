package org.example.springcrypto.dto;

public record ShareSecretRequest(
    Long secretId,           // ID del secreto a compartir
    Long sharedWithUserId,   // ID del usuario con quien compartir
    String encryptedData,    // Secreto cifrado con la clave pública del receptor (Base64)
    String permission,       // "READ" o "READ_WRITE"
    String algorithm,        // "RSA" o "EC"
    Long expiresInDays       // Opcional: días hasta que expire el compartido
) {
}

