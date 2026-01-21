package org.example.springcrypto.dto;

import java.time.LocalDateTime;

public record SharedSecretItem(
    Long shareId,
    Long secretId,
    Long ownerId,           // Quien compartió
    Long sharedWithId,      // Con quien se compartió
    String encryptedData,   // Base64 - cifrado con clave pública
    String permission,
    String algorithm,
    LocalDateTime createdAt,
    LocalDateTime expiresAt,
    boolean isExpired
) {
}

