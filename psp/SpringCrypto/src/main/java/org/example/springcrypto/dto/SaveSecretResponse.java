package org.example.springcrypto.dto;

import java.time.LocalDateTime;

public record SaveSecretResponse(
    Long secretId,
    LocalDateTime createdAt
) {
}

