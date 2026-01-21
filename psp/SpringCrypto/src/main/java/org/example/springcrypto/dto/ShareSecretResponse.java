package org.example.springcrypto.dto;

import java.time.LocalDateTime;

public record ShareSecretResponse(
    Long shareId,
    Long secretId,
    Long sharedWithUserId,
    String permission,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {
}

