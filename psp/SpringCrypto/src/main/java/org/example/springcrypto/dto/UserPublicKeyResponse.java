package org.example.springcrypto.dto;

public record UserPublicKeyResponse(
    Long userId,
    String publicKey,  // Base64
    String algorithm,
    Integer keySize
) {
}

