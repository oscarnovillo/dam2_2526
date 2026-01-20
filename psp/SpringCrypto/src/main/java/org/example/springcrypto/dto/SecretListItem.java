package org.example.springcrypto.dto;

import java.time.LocalDateTime;

public record SecretListItem(
    Long id,
    String metadata,      // Título cifrado
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}

